/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2016, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.geotools.data.hana;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.PreparedFilterToSQL;
import org.geotools.jdbc.PreparedStatementSQLDialect;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ByteOrderValues;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

/**
 * SQLDialect- The driver used by JDBCDataStore to directly communicate with the
 * database. This class encapsulates all the database specific operations that
 * JDBCDataStore needs to function. It is implemented on a per-database basis.
 * PreparedStatementSQLDialect- SUbclass of SQLDialect SQL dialect uses prepared
 * statements for database interaction.
 * 
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */

public class HanaDialect extends PreparedStatementSQLDialect {

public static final String SRS_MIN_X = "srsMinX";

public static final String SRS_MAX_X = "srsMaxX";

public static final String SRS_MIN_Y = "srsMinY";

public static final String SRS_MAX_Y = "srsMaxY";

public static final String SRS_IS_ROUND_EARTH = "srsIsRoundEarth";

private static String POINT_STR = "ST_POINT";

private static String GEOMETRY_STR = "ST_GEOMETRY";

/**
 * Pre-set SAP HANA specific SQL Statements.
 */

private static String SELECT_SRSID_WITH_SCHEMA = "SELECT srs_id "
        + "FROM PUBLIC.ST_GEOMETRY_COLUMNS " + "WHERE schema_name = ? "
        + "AND table_name = ? " + "AND column_name = ?";

private static String SELECT_SRSID_WITHOUT_SCHEMA = "SELECT SRS_ID "
        + "FROM PUBLIC.ST_GEOMETRY_COLUMNS " + "WHERE schema_name = USER "
        + "AND table_name = ? " + "AND column_name = ?";

private static String SELECT_CRS_WKT = "SELECT definition, organization, organization_coordsys_id "
        + "FROM PUBLIC.ST_SPATIAL_REFERENCE_SYSTEMS " + "WHERE srs_id= ?";

private String SELECT_SRS_PROPERTIES_FROM_ID = "SELECT min_x, max_x, min_y, max_y, CASE round_earth "
        + "WHEN 'TRUE'THEN 1 ELSE 0 END "
        + "FROM PUBLIC.ST_SPATIAL_REFERENCE_SYSTEMS " + "WHERE srs_id = ?";

private static String SELECT_INCLUDE_WITH_SCHEMA = "SELECT schema_name, table_name "
        + "FROM PUBLIC.ST_GEOMETRY_COLUMNS " + "WHERE schema_name = ? "
        + "AND table_name = ?";

private static String SELECT_INCLUDE = "SELECT schema_name, table_name "
        + "FROM PUBLIC.ST_GEOMETRY_COLUMNS " + "WHERE schema_name = USER "
        + "AND table_name = ?";

private static String SELECT_GEOMETRY_TYPE_WITH_SCHEMA = "SELECT data_type_name "
        + "FROM PUBLIC.ST_GEOMETRY_COLUMNS " + "WHERE schema_name = ? "
        + "AND table_name = ? " + "AND column_name = ?";

private static String SELECT_GEOMETRY_TYPE = "SELECT data_type_name "
        + "FROM SYS.ST_GEOMETRY_COLUMNS " + "WHERE schema_name = USER "
        + "AND table_name = ? " + "AND column_name = ?";

/**
 * Instantiates a new Hana dialect.
 */

public HanaDialect(JDBCDataStore dataStore) {
    super(dataStore);
}

/**
 * Prepare a filter parser/encoder object.
 */

@Override
public PreparedFilterToSQL createPreparedFilterToSQL() {
    HanaFilterToSQL filter = new HanaFilterToSQL(this);
    return filter;
}

/**
 * Returns the spatial reference system identifier (srid) for a particular
 * geometry column. In the event that the srid cannot be determined, this method
 * should return null.
 */

@Override
public Integer getGeometrySRID(String schemaName, String tableName,
        String columnName, Connection cx) throws SQLException {
    Integer srid = null;
    PreparedStatement stmt = null;

    try {
        if (schemaName != null) {
            stmt = cx.prepareStatement(SELECT_SRSID_WITH_SCHEMA);
            stmt.setString(1, schemaName);
            stmt.setString(2, tableName);
            stmt.setString(3, columnName);
        } else {
            stmt = cx.prepareStatement(SELECT_SRSID_WITHOUT_SCHEMA);
            stmt.setString(1, tableName);
            stmt.setString(2, columnName);
        }
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();
            if (rs.next()) {
                srid = (Integer) rs.getObject(1);
                // boolean hasNext = rs.next();
                // if (hasNext) {
                // throw new AssertionError("Unexpected part of code
                // reached: While finding SRID of a checking if a given
                // table has geometry columns, more than one entry found");
                // }
            }
        } finally {
            dataStore.closeSafe(rs);
        }
    } finally {
        dataStore.closeSafe(stmt);
    }
    return srid;
}

/**
 * Turns the specified srid into a CoordinateReferenceSystem, or returns null if
 * not possible. The implementation might just use CRS.decode("EPSG:" + srid),
 * but most spatial databases will have their own SRS database that can be
 * queried as well. Decodes the official EPSG code first, and fall back on the
 * custom database definition otherwise. Find the SRS definition from a SAP HANA
 * SRID.
 */

@Override
public CoordinateReferenceSystem createCRS(int srid, Connection cx)
        throws SQLException {

    String wkt = null;
    int orgid = 0;
    String org = null;

    CoordinateReferenceSystem srs = null;
    PreparedStatement ps = null;

    /**
     * First, look up the WKT definition of the SRS to find a official
     * definition from the issuing organization.
     */

    try {
        ps = cx.prepareStatement(SELECT_CRS_WKT);
        ps.setInt(1, srid);
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
            if (rs.next()) {
                wkt = rs.getString(1);
                org = rs.getString(2);
                orgid = rs.getInt(3);
            }
        } finally {
            dataStore.closeSafe(rs);
        }
    } finally {
        dataStore.closeSafe((Statement) ps);
    }

    /**
     * If we have a WKT definition, try to decode with CRS.parseWKT, on success
     * return the SRS definition.
     */

    if (wkt != null) {
        try {
            srs = CRS.parseWKT((String) wkt);
        } catch (Exception e) {
            /**
             * This is Executed for SRID 0 in SAP HANA.
             */

            LOGGER.warning("Found Empty WKT Definiton of SAP HANA for SRS "
                    + srid + ". .");
        }
        if (srs != null) {
            return srs;
        }
    }

    /**
     * If we have the organization name and id, look up the SRS definition from
     * GeoTools predefined list.
     */

    if (orgid != 0 && org != null) {
        try {
            srs = CRS.decode((String) (String.valueOf(org) + ":" + orgid));
        } catch (Exception e) {
            LOGGER.warning("Could not decode SRS " + org + ":" + orgid
                    + " using the geotools database");
        }
        if (srs != null) {
            return srs;
        }
    }

    return null;
}

/**
 * Using SAP HANA specific SQL statement, finds out the data_type_name from
 * PUBLIC.ST_GEOMETRY_COLUMNS
 */

public String getNativeGeometryType(String geomColumnName, String tableName,
        String schemaName, Connection cx) throws SQLException {
    PreparedStatement ps = null;
    try {
        if (schemaName != null) {
            ps = cx.prepareStatement(SELECT_GEOMETRY_TYPE_WITH_SCHEMA);
            ps.setString(1, schemaName);
            ps.setString(2, tableName);
            ps.setString(3, geomColumnName);
        } else {
            ps = cx.prepareStatement(SELECT_GEOMETRY_TYPE);
            ps.setString(1, tableName);
            ps.setString(2, geomColumnName);
        }
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } finally {
            dataStore.closeSafe(rs);
        }
    } finally {
        dataStore.closeSafe(ps);
    }

    throw new AssertionError(
            "Unexpected part of code reached: 'data_type_name' not found in PUBLIC.ST_GEOMETRY_COLUMNS' in HANA");
}

/**
 * Determines if the specified table should be included in those published by
 * the datastore. This method returns true if the table should be published as a
 * feature type, otherwise it returns false. Only include tables in the SAP HANA
 * Schema with geometry columns.
 */

@Override
public boolean includeTable(String schemaName, String tableName, Connection cx)
        throws SQLException {
    boolean isGeomTable = true;
    PreparedStatement ps = null;
    try {
        if (schemaName != null && schemaName.trim().length() > 0) {
            ps = cx.prepareStatement(SELECT_INCLUDE_WITH_SCHEMA);
            ps.setString(1, schemaName);
            ps.setString(2, tableName);
        } else {
            ps = cx.prepareStatement(SELECT_INCLUDE);
            ps.setString(1, tableName);
        }
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
            isGeomTable = rs.next();
            // if (isGeomTable) {
            // boolean hasNext = rs.next();
            // if (hasNext) {
            // throw new AssertionError("Unexpected part of code reached:
            // While checking if a given table has geometry columns, more
            // than one entry found");
            // }
            // }
        } finally {
            dataStore.closeSafe(rs);
        }
    } finally {
        dataStore.closeSafe(ps);
    }
    return isGeomTable;
}

/**
 * Returns the bounds of all geometry columns in the layer using any approach
 * that proves to be faster than the plain bounds aggregation, or null if none
 * exists or the fast method has not been enabled. Do work-around for
 * round-earth data because SAP HANA currently doesn't support envelope
 * aggregation functions in round-earth.
 * 
 * @return a list of referenced envelopes (some of which may be null or empty)
 */

@Override
public List<ReferencedEnvelope> getOptimizedBounds(String schema,
        SimpleFeatureType featureType, Connection cx)
                throws SQLException, IOException {
    boolean isRoundEarth = ((Boolean) featureType.getUserData()
            .get(SRS_IS_ROUND_EARTH)).booleanValue();

    if (!isRoundEarth) {
        return getBoundsForPlanar(schema, featureType, cx);
    } else {
        /**
         * Use default implementation for round earth data, which does a
         * sequential scan.
         */

        return null;
    }
}

/**
 * Calculating planar bounds by doing an aggregate SQL call on the geometry
 * column.
 */

private List<ReferencedEnvelope> getBoundsForPlanar(String schema,
        SimpleFeatureType featureType, Connection cx)
                throws SQLException, IOException {

    List<ReferencedEnvelope> bounds = new ArrayList<ReferencedEnvelope>();

    String tableName = featureType.getTypeName();
    String geomColumnName = featureType.getGeometryDescriptor().getName()
            .toString();
    CoordinateReferenceSystem flatCRS = CRS
            .getHorizontalCRS(featureType.getCoordinateReferenceSystem());

    /**
     * HANA specific SQL statement ST_EnvelopeAggr(<geometry_column>) Returns a
     * polygon that is the bounding rectangle for all the geometries in a group.
     */

    StringBuffer sql = new StringBuffer("SELECT ST_EnvelopeAggr");
    sql.append("(");
    encodeColumnName(null, geomColumnName, sql);
    sql.append(")");

    sql.append(" FROM ");

    if (schema != null) {
        // Encodes the name of a schema in an SQL statement.
        encodeSchemaName(schema, sql);
        sql.append(".");
    }
    // Encodes the name of a table in an SQL statement.
    encodeTableName(tableName, sql);

    PreparedStatement ps = null;
    try {
        ps = cx.prepareStatement(sql.toString());
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
            while (rs.next()) {
                // Parse WKB representation of geometry coming from SAP HANA
                // when called with String name.
                Envelope envelope = decodeGeometryEnvelope(rs, 1, cx);
                // Then wraps the geometry into a bounding box.
                if (envelope != null) {
                    if (envelope instanceof ReferencedEnvelope) {
                        bounds.add((ReferencedEnvelope) envelope);
                    } else {
                        bounds.add(new ReferencedEnvelope(envelope, flatCRS));
                    }
                }
            }
        } finally {
            dataStore.closeSafe(rs);
        }
    } finally {
        dataStore.closeSafe(ps);
    }
    return bounds;
}

/**
 * Decodes the result of a spatial extent function in a SELECT statement. This
 * method is given direct access to a result set. The column parameter is the
 * index into the result set which contains the spatial extent value. The query
 * for this value is build with the encodeGeometryEnvelope(String, String,
 * StringBuffer) method. This method must not read any other objects from the
 * result set other then the one referenced by column.
 */

/**
 * Parses WKB representation of geometry coming from SAP HANA.
 */

@Override
public Envelope decodeGeometryEnvelope(ResultSet rs, int column, Connection cx)
        throws SQLException, IOException {
    byte[] wkb = rs.getBytes(column);
    try {
        if (wkb != null) {
            Geometry geom = new WKBReader().read(wkb);
            return geom.getEnvelopeInternal();
        }
        return new Envelope();
    } catch (ParseException e) {
        String msg = "Error decoding WKB for envelope";
        throw (IOException) new IOException(msg).initCause((Throwable) e);
    }
}

/**
 * Decodes the geometry value from the result of a query on SAP HANA specifying
 * the column name as String. Executed when previewing the layer.
 */

@Override
public Geometry decodeGeometryValue(GeometryDescriptor descriptor, ResultSet rs,
        String name, GeometryFactory factory, Connection cx)
                throws IOException, SQLException {
    byte[] bytes = rs.getBytes(name);
    return decodeGeometryValueFromBytes(factory, bytes);
}

/**
 * Decodes the geometry value from the result of a query on SAP HANA specifying
 * the column as an index. Executed when previewing the layer.
 */

@Override
public Geometry decodeGeometryValue(GeometryDescriptor descriptor, ResultSet rs,
        int column, GeometryFactory factory, Connection cx)
                throws IOException, SQLException {
    byte[] bytes = rs.getBytes(column);
    return decodeGeometryValueFromBytes(factory, bytes);
}

/**
 * Parse WKB representation of geometry coming from the result of a query on SAP
 * HANA.
 */

private Geometry decodeGeometryValueFromBytes(GeometryFactory factory,
        byte[] bytes) throws IOException {
    if (bytes == null) {
        return null;
    }
    try {
        return new WKBReader(factory).read(bytes);
    } catch (ParseException e) {
        String msg = "Error decoding WKB";
        throw (IOException) new IOException(msg).initCause((Throwable) e);
    }
}

/**
 * Encodes the spatial extent function of a geometry column in a SELECT
 * statement. Used by default implementation to get bounds. Used by round-earth
 * column only, and the columnName itself is used as the filter to return all
 * geometries in the column. Bounds are then calculated by GeoTools internally.
 */

@Override
public void encodeGeometryEnvelope(String tableName, String geometryColumn,
        StringBuffer sql) {
    encodeColumnName(null, geometryColumn, sql);
}

/**
 * Handles type casting. Prepares a function argument for a prepared statement.
 */

private static Map<Class<?>, String> CAST_EXPPRESSIONS = null;

static {
    CAST_EXPPRESSIONS = new HashMap<Class<?>, String>();
    CAST_EXPPRESSIONS.put(Short.class, "TO_SMALLINT(?)");
    CAST_EXPPRESSIONS.put(Integer.class, "TO_INTEGER(?)");
    CAST_EXPPRESSIONS.put(Long.class, "TO_BIGINT(?)");
    CAST_EXPPRESSIONS.put(BigDecimal.class, "TO_DECIMAL(?, 34");
    CAST_EXPPRESSIONS.put(Float.class, "TO_REAL(?)");
    CAST_EXPPRESSIONS.put(Double.class, "TO_DOUBLE(?)");
    CAST_EXPPRESSIONS.put(java.sql.Date.class, "TO_DATE('?', 'YYYY-MM-DD')");
    CAST_EXPPRESSIONS.put(Date.class, "TO_DATE('?', 'YYYY-MM-DD')");
    CAST_EXPPRESSIONS.put(Time.class, "TO_TIME ('?', 'HH:MI AM')");
    CAST_EXPPRESSIONS.put(Timestamp.class,
            "TO_TIMESTAMP ('?', 'YYYY-MM-DD HH24:MI:SS')");
    CAST_EXPPRESSIONS.put(String.class,
            "TO_VARCHAR (TO_DATE('?'), 'YYYY/MM/DD')");
    CAST_EXPPRESSIONS.put(Clob.class, "TO_CLOB(?)");
    CAST_EXPPRESSIONS.put(Blob.class, "TO_BLOB(?)");
}

@Override
public void prepareFunctionArgument(Class clazz, StringBuffer sql) {
    String castExpression = CAST_EXPPRESSIONS.get(clazz);
    if (castExpression != null)
        sql.append(castExpression);
    else
        super.prepareFunctionArgument(clazz, sql);
}

/**
 * Registers the sql type name to java type mappings. Dialect uses this when
 * reading and writing objects to and from the database.
 */

@Override
public void registerSqlTypeNameToClassMappings(Map<String, Class<?>> mappings) {
    super.registerSqlTypeNameToClassMappings(mappings);
    mappings.put(POINT_STR, Point.class);
    mappings.put(GEOMETRY_STR, Geometry.class);
}

/**
 * Determines the name of the sequence (if any) which is used to increment
 * generate values for a table column. This method should return null if no such
 * sequence exists. This is executed when publishing a new layer from a table.
 */

@Override
public String getSequenceForColumn(String schemaName, String tableName,
        String columnName, Connection cx) throws SQLException {
    return null;
}

/**
 * Obtains the next value of a sequence, incrementing the sequence to the next
 * state in the process. Implementations should determine the next value of a
 * column for which values are automatically generated by the database.
 * Implementations should handle the case where schemaName is null.
 */

@Override
public Object getNextSequenceValue(String schemaName, String sequenceName,
        Connection cx) throws SQLException {
    return null;
}

/**
 * Sets a value in a prepared statement, for "basic types" (non-geometry).
 * Subclasses should override this method if they need to do something custom or
 * they wish to support non-standard types. Here, it is a work around for
 * integer values in scientific form. For example, this is executed when running
 * a CQL filter with integers.
 */

@Override
public void setValue(Object value, Class binding, PreparedStatement ps,
        int column, Connection cx) throws SQLException {
    // Gets the sql type
    Integer sqlType = dataStore.getMapping(binding);

    // Handles integer in scientific forms (eg. uDig generated styles)
    if (value != null && sqlType == Types.INTEGER) {
        int val = new BigDecimal(value.toString()).intValue();
        ps.setInt(column, val);
    } else {
        super.setValue(value, binding, ps, column, cx);
    }
}

public JDBCDataStore getDataStore() {
    return dataStore;
}

/**
 * Customization to the CREATE TABLE statement for SAP HANA
 */

@Override
public void encodeCreateTable(StringBuffer sql) {
    sql.append("CREATE COLUMN TABLE ");
}

/**
 * Encodes anything post a column in a CREATE TABLE statement. This is appended
 * after the column name and type. Adds the SRID after the geometry column when
 * creating a table Note that the SRID entered from Geoserver must be installed
 * on HANA to avoid error.
 */

@Override
public void encodePostColumnCreateTable(AttributeDescriptor att,
        StringBuffer sql) {
    if (att instanceof GeometryDescriptor) {
        GeometryDescriptor gDescr = (GeometryDescriptor) att;
        CoordinateReferenceSystem crs = gDescr.getCoordinateReferenceSystem();
        Set<ReferenceIdentifier> identifiers = crs.getIdentifiers();
        Integer srsId = null;
        for (ReferenceIdentifier ident : identifiers) {
            if (ident.getCodeSpace().equals("EPSG")) {
                try {
                    srsId = Integer.parseInt(ident.getCode());
                } catch (NumberFormatException e) {
                }
            }
        }
        if (srsId == null) {
            throw new RuntimeException("Unknown SRID");
        }
        sql.append("(" + srsId + ")");
    }
}

/**
 * Callback which executes after a feature type has been built from a database
 * table. Add the metadata for SRS extends and round-earth/planar to the layer
 * definition, when the layer is created from a SAP HANA table.
 */

@Override
public void postCreateFeatureType(SimpleFeatureType featureType,
        DatabaseMetaData metadata, String schemaName, Connection cx)
                throws SQLException {

    GeometryDescriptor gDescr = featureType.getGeometryDescriptor();
    Integer srsId = (Integer) gDescr.getUserData()
            .get(JDBCDataStore.JDBC_NATIVE_SRID);
    // Update the nature about the spatial reference system
    featureType.getUserData().put(SRS_MIN_X, -Double.MAX_VALUE);
    featureType.getUserData().put(SRS_MAX_X, -Double.MAX_VALUE);
    featureType.getUserData().put(SRS_MIN_Y, Double.MAX_VALUE);
    featureType.getUserData().put(SRS_MAX_Y, Double.MAX_VALUE);
    featureType.getUserData().put(SRS_IS_ROUND_EARTH, true);

    if (srsId != null && srsId > -1) {
        PreparedStatement ps = null;
        try {
            ps = cx.prepareStatement(SELECT_SRS_PROPERTIES_FROM_ID);
            ps.setInt(1, srsId);
            ResultSet rs = null;
            try {
                rs = ps.executeQuery();
                if (rs.next()) {
                    double min_x = rs.getDouble(1);
                    double max_x = rs.getDouble(2);
                    double min_y = rs.getDouble(3);
                    double max_y = rs.getDouble(4);
                    boolean isRoundEarth = rs.getBoolean(5);

                    // do special check here for if minimum and maximum are
                    // swapped
                    if (min_x > max_x) {
                        double temp_x = min_x;
                        min_x = max_x;
                        max_x = temp_x;
                    }
                    if (min_y > max_y) {
                        double temp_y = min_y;
                        min_y = max_y;
                        max_y = temp_y;
                    }

                    // allow 50% displacement for SRS boundary
                    double offset_x = (max_x - min_x) / 2.0;
                    double offset_y = (max_y - min_y) / 2.0;
                    min_x -= offset_x;
                    max_x += offset_x;
                    min_y -= offset_y;
                    max_y += offset_y;

                    // set variables to layer definition
                    featureType.getUserData().put(SRS_MIN_X, min_x);
                    featureType.getUserData().put(SRS_MAX_X, max_x);
                    featureType.getUserData().put(SRS_MIN_Y, min_y);
                    featureType.getUserData().put(SRS_MAX_Y, max_y);
                    featureType.getUserData().put(SRS_IS_ROUND_EARTH,
                            isRoundEarth);
                }
            } finally {
                dataStore.closeSafe(rs);
            }
        } finally {
            dataStore.closeSafe(ps);
        }
    }
}

@Override
protected boolean supportsSchemaForIndex() {
    return true;
}

@Override
public boolean isLimitOffsetSupported() {
    return true;
}

@Override
public void applyLimitOffset(StringBuffer sql, int limit, int offset) {
    if (limit >= 0 && limit < Integer.MAX_VALUE) {
        if (offset > 0)
            sql.append(" LIMIT " + offset + ", " + limit);
        else
            sql.append(" LIMIT " + limit);
    } else if (offset > 0) {
        // HANA pretends to have limit specified along with offset
        sql.append(" LIMIT " + offset + ", " + Long.MAX_VALUE);
    }
}

/**
 * Sets the geometry value into the prepared statement.
 */

@SuppressWarnings("unchecked")
@Override
public void setGeometryValue(Geometry g, int srid, int tmp, Class binding,
        PreparedStatement ps, int column) throws SQLException {
    if (g == null) {
        ps.setBytes(column, null);
        return;
    }
    int dims = 2;
    final Coordinate[] cs = g.getCoordinates();
    for (int t = cs.length - 1; t >= 0; t--) {
        if (!(Double.isNaN(cs[t].z))) {
            dims = 3;
            break;
        }
    }

    WKBWriter w = new WKBWriter(dims, ByteOrderValues.LITTLE_ENDIAN);
    byte[] bytes = w.write(g);
    ps.setBytes(column, bytes);
}

@Override
public void encodeGeometryColumn(GeometryDescriptor gatt, String prefix,
        int srid, StringBuffer sql) {

    encodeColumnName(prefix, gatt.getLocalName(), sql);
    sql.append(".ST_AsWKB()");
}

/**
 * Prepares the geometry value for a prepared statement. Wraps the geometry
 * placeholder in the function. The default implementation just appends the
 * default placeholder: '?'.
 */

@SuppressWarnings("unchecked")
public void prepareGeometryValue(Geometry geom, int srid, Class binding,
        StringBuffer sql) {
    String pattern = null;
    if (geom != null) {
        if (srid > -1) {
            pattern = "ST_GeomFromWKB( ? ,{0})";
            sql.append(MessageFormat.format(pattern, Integer.toString(srid)));
        } else {
            sql.append("ST_GeomFromWKB( ? )");
        }
    } else {
        sql.append("?");
    }
}

}