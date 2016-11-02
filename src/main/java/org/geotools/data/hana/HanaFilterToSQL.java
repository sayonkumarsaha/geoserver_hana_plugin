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

//Yet to be reviewed

package org.geotools.data.hana;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.geotools.data.Transaction;
import org.geotools.data.jdbc.FilterToSQLException;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.function.FilterFunction_strConcat;
import org.geotools.filter.function.FilterFunction_strEndsWith;
import org.geotools.filter.function.FilterFunction_strEqualsIgnoreCase;
import org.geotools.filter.function.FilterFunction_strIndexOf;
import org.geotools.filter.function.FilterFunction_strLength;
import org.geotools.filter.function.FilterFunction_strReplace;
import org.geotools.filter.function.FilterFunction_strStartsWith;
import org.geotools.filter.function.FilterFunction_strSubstring;
import org.geotools.filter.function.FilterFunction_strSubstringStart;
import org.geotools.filter.function.FilterFunction_strToLowerCase;
import org.geotools.filter.function.FilterFunction_strToUpperCase;
import org.geotools.filter.function.FilterFunction_strTrim;
import org.geotools.filter.function.FilterFunction_strTrim2;
import org.geotools.filter.function.math.FilterFunction_abs;
import org.geotools.filter.function.math.FilterFunction_abs_2;
import org.geotools.filter.function.math.FilterFunction_abs_3;
import org.geotools.filter.function.math.FilterFunction_abs_4;
import org.geotools.filter.function.math.FilterFunction_ceil;
import org.geotools.filter.function.math.FilterFunction_floor;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.PreparedFilterToSQL;
import org.geotools.jdbc.PreparedStatementSQLDialect;
import org.geotools.jdbc.SQLDialect;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.DistanceBufferOperator;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import org.opengis.filter.temporal.After;
import org.opengis.filter.temporal.Before;
import org.opengis.filter.temporal.Begins;
import org.opengis.filter.temporal.BegunBy;
import org.opengis.filter.temporal.During;
import org.opengis.filter.temporal.EndedBy;
import org.opengis.filter.temporal.Ends;
import org.opengis.filter.temporal.TContains;
import org.opengis.filter.temporal.TEquals;
import org.opengis.filter.temporal.TOverlaps;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * Class FilterToSQL: Encodes a filter into a SQL WHERE statement. Generic
 * enough that any SQL database works with it. This generic SQL encoder should
 * eventually be able to encode all filters except Geometry Filters. This is
 * because the OGC's SFS for SQL document specifies two ways of doing SQL
 * databases, one with native geometry types and one without. To implement an
 * encoder for one of the two types simply subclass off of this encoder and put
 * in the proper GeometryFilter visit method. Then add the filter types
 * supported to the capabilities by overriding the {createFilterCapabilities()
 * method. This version was ported from the original to support
 * org.opengis.filter type Filters. class PreparedFilterToSQL: Extension of
 * FilterToSQL intended for use with prepared statements. Each time a Literal is
 * visited, a '?' is encoded, and the value and type of the literal are stored,
 * available after the fact via getLiteralValues() and getLiteralTypes().
 *
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */

@SuppressWarnings("deprecation")
public class HanaFilterToSQL extends PreparedFilterToSQL {

private static Logger LOGGER = org.geotools.util.logging.Logging
        .getLogger("org.geotools.data.hana");

static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

static SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss.SSS");

static {
    // Set DATE_FORMAT time zone to GMT, as Date's are always in GMT
    // internalLy. Otherwise we'll get a local time-zone encoding regardless
    // of the actual Date value
    DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
}

protected HanaDialect dialect;

/**
 * Interface Hierarchy For Package org.opengis.filter.spatial
 * BinarySpatialOperator BBOX BBOX3D Contains (also extends
 * BoundedSpatialOperator) Crosses (also extends BoundedSpatialOperator)
 * Disjoint DistanceBufferOperator Beyond DWithin Equals (also extends
 * BoundedSpatialOperator) Intersects (also extends BoundedSpatialOperator)
 * Overlaps (also extends BoundedSpatialOperator) Touches (also extends
 * BoundedSpatialOperator) Within (also extends BoundedSpatialOperator)
 * BoundedSpatialOperator Contains (also extends BinarySpatialOperator) Crosses
 * (also extends BinarySpatialOperator) Equals (also extends
 * BinarySpatialOperator) Intersects (also extends BinarySpatialOperator)
 * Overlaps (also extends BinarySpatialOperator) Touches (also extends
 * BinarySpatialOperator) Within (also extends BinarySpatialOperator)
 */

private boolean functionEncodingEnabled = false;

private boolean isGeomLiteralValid = true;

/**
 * Default constructor
 */

public HanaFilterToSQL(PreparedStatementSQLDialect dialect) {
    super(dialect);
    dialect = (HanaDialect) dialect;
}

public HanaFilterToSQL(Writer out) {
    super(out);
}

/**
 * Sets the filter capabilities.
 * 
 * @return the filter capabilities
 */

@Override
protected FilterCapabilities createFilterCapabilities() {
    FilterCapabilities caps = new FilterCapabilities();

    // adding the simple arithmetic and comparison operations
    caps.addAll(SQLDialect.BASE_DBMS_CAPABILITIES);

    // adding the OpenGIS Spatial Filter support
    caps.addType(BBOX.class);
    caps.addType(Disjoint.class);
    caps.addType(Equals.class);
    caps.addType(Intersects.class);

    /**
     * Methods that cannot be used with geometries in round-Earth spatial
     * reference system: ST_Area, ST_Boundary, ST_Buffer, ST_Centroid,
     * ST_ConvexHull, ST_Relate, ST_Envelop, ST_Crosses, ST_Contains,
     * ST_Overlap, ST_Touches, ST_Within, ST_Distance and ST_WithinDistance
     * (partially supported. Only point-point)
     */

    // Spatial capabilities that are planar exclusive for SAP HANA.
    boolean isRoundEarth = false;
    if (featureType != null)
        isRoundEarth = ((Boolean) featureType.getUserData()
                .get(HanaDialect.SRS_IS_ROUND_EARTH)).booleanValue();

    if (!isRoundEarth) {
        caps.addType(Overlaps.class);
        caps.addType(Contains.class);
        caps.addType(Touches.class);
        caps.addType(Crosses.class);
        caps.addType(Within.class);
        // ST_WithinDistance partially supported. Only point-point
        caps.addType(DWithin.class);
        // ST_Distance partially supported. Only point-point
        caps.addType(Beyond.class);
    }

    // adding the OpenGIS Temporal Filter support
    caps.addType(After.class);
    caps.addType(Before.class);
    caps.addType(Begins.class);
    caps.addType(BegunBy.class);
    caps.addType(During.class);
    caps.addType(TContains.class);
    caps.addType(TOverlaps.class);
    caps.addType(Ends.class);
    caps.addType(EndedBy.class);
    caps.addType(TEquals.class);

    if (isFunctionEncodingEnabled()) {

        // adding the OpenGIS String Functions support
        caps.addType(FilterFunction_strConcat.class);
        caps.addType(FilterFunction_strEndsWith.class);
        caps.addType(FilterFunction_strStartsWith.class);
        caps.addType(FilterFunction_strEqualsIgnoreCase.class);
        caps.addType(FilterFunction_strIndexOf.class);
        caps.addType(FilterFunction_strLength.class);
        caps.addType(FilterFunction_strToLowerCase.class);
        caps.addType(FilterFunction_strToUpperCase.class);
        caps.addType(FilterFunction_strReplace.class);
        caps.addType(FilterFunction_strSubstring.class);
        caps.addType(FilterFunction_strSubstringStart.class);
        caps.addType(FilterFunction_strTrim.class);
        caps.addType(FilterFunction_strTrim2.class);

        // adding the OpenGIS Math Functions support
        caps.addType(FilterFunction_abs.class);
        caps.addType(FilterFunction_abs_2.class);
        caps.addType(FilterFunction_abs_3.class);
        caps.addType(FilterFunction_abs_4.class);
        caps.addType(FilterFunction_ceil.class);
        caps.addType(FilterFunction_floor.class);
    }
    return caps;
}

/**
 * Performs the encoding, sends the encoded sql to the writer passed in. Throws:
 * OpenGISFilterToOpenGISFilterToSQLEncoderException -If filter type not
 * supported, or if there were io problems. Override encode function for better
 * exception string for round-earth data.
 */

@Override
public void encode(Filter filter) throws FilterToSQLException {
    if (out == null)
        throw new FilterToSQLException("Can't encode to a null writer.");
    if (getCapabilities().fullySupports(filter))
        super.encode(filter);
    else {
        boolean isRoundEarth = false;
        if (featureType != null)
            isRoundEarth = ((Boolean) featureType.getUserData()
                    .get(HanaDialect.SRS_IS_ROUND_EARTH)).booleanValue();

        if (isRoundEarth)
            throw new FilterToSQLException(
                    "Filter type not supported by SAP HANA for round-earth data");
        else
            throw new FilterToSQLException("Filter type not supported");
    }
}

/**
 * Type Checking: Check if {Literal} is {Date Literal}.
 *
 * @param expression the expression
 * @param target the target
 * @return the object
 */

@SuppressWarnings("unchecked")
@Override
protected Object evaluateLiteral(Literal expression, Class target) {
    if (target != null) {
        if (java.sql.Date.class.isAssignableFrom(target)) {
            Date date = (Date) expression.getValue();
            java.sql.Date literal = new java.sql.Date(date.getTime());
            return literal;
        }
    }

    return super.evaluateLiteral(expression, target);
}

/**
 * Gets the srid.
 *
 * @return the srid
 */

private Integer getSRID() {
    return getSRID(featureType.getGeometryDescriptor());
}

/**
 * Gets the srid.
 *
 * @param gDescr the g descr
 * @return the srid
 */

private Integer getSRID(GeometryDescriptor gDescr) {
    Integer result = null;
    if (gDescr != null)
        result = (Integer) gDescr.getUserData()
                .get(JDBCDataStore.JDBC_NATIVE_SRID);

    if (result == null)
        result = currentSRID;
    return result;
}

public boolean isFunctionEncodingEnabled() {
    return functionEncodingEnabled;
}

private void resetGeomLiteralValidity() {
    isGeomLiteralValid = true;
}

public void setFunctionEncodingEnabled(boolean functionEncodingEnabled) {
    functionEncodingEnabled = functionEncodingEnabled;
}

/**
 * for round-earth data, we have to first test create the geometry literal in
 * the database, because a single round-earth geometry cannot be larger than
 * half the hemisphere.
 *
 * @param definition the definition
 * @param min_x the min_x
 * @param max_x the max_x
 * @return true, if successful
 */

private boolean testCreateGeometry(String definition, double min_x,
        double max_x) {
    // first check if the x-coordinates are larger than half the world
    double length = max_x - min_x;
    if (length >= 180) {
        return false;
    }

    // then check if the geometry can be created by SAP HANA
    boolean result = true;
    Connection cx = null;
    try {
        cx = dialect.getDataStore().getConnection(Transaction.AUTO_COMMIT);
        PreparedStatement ps = null;
        try {
            ps = cx.prepareStatement("SELECT " + definition);
            ps.execute();
            ResultSet rs = null;
            try {
                rs = ps.executeQuery();
                if (!rs.next()) {
                    return false;
                }
            } catch (SQLException e) {
                result = false;
            } finally {
                dialect.getDataStore().closeSafe(rs);
            }
        } catch (SQLException e) {
            result = false;
        } finally {
            dialect.getDataStore().closeSafe(ps);
        }
    } catch (IOException e) {
        LOGGER.warning(
                "Cannot varify if the filter geometry is valid under round-earth.");
    } finally {
        dialect.getDataStore().closeSafe(cx);
    }

    return result;
}

/**
 * Mapping the most frequently used abbreviations to SAP HANA native units of
 * measurement strings, extend the mappings when needed.
 *
 * @param units the units
 * @return the string
 */

private String unitsOfMeasureMapping(String units) {
    if (units.equals("kilometer"))
        return "kilometre";
    else if (units.equals("km"))
        return "kilometre";
    else if (units.equals("m"))
        return "metre";
    else if (units.equals("ft"))
        return "foot";
    else if (units.equals("feet"))
        return "foot";
    else if (units.equals("yd"))
        return "yard";
    else if (units.equals("mile"))
        return "US survey mile";
    else if (units.equals("l"))
        return "link";
    else if (units.equals("li"))
        return "link";
    else if (units.equals("lnk"))
        return "link";
    else if (units.equals("rad"))
        return "radian";
    else if (units.equals("mrad"))
        return "microradian";

    return units;
}

/**
 * Exclude everything. Use a dummy expression 1=0
 *
 * @param filter the filter
 * @param extraData the extra data
 * @return the object
 */

@Override
public Object visit(ExcludeFilter filter, Object extraData) {
    try {
        out.write("1 = 0");
    } catch (java.io.IOException ioe) {
    }
    ;

    return extraData;
}

/**
 * Parse: {Function}( {Expression}, {Expression} ).
 *
 * @param function the function
 * @param extraData the extra data
 * @return the object
 * @throws RuntimeException the runtime exception
 */

@Override
public Object visit(Function function, Object extraData)
        throws RuntimeException {
    try {
        encodingFunction = false;
        boolean encoded = visitFunction(function, extraData);

        if (encoded) {
            return extraData;
        } else {
            return super.visit(function, extraData);
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}

/**
 * Include everything. Use a dummy expression 1=1
 *
 * @param filter the filter
 * @param extraData the extra data
 * @return the object
 */

@Override
public Object visit(IncludeFilter filter, Object extraData) {
    try {
        out.write("1 = 1");
    } catch (java.io.IOException ioe) {
    }
    ;
    return extraData;
}

/**
 * Parse: {Literal}.
 *
 * @param expression the expression
 * @param context the context
 * @return the object
 * @throws RuntimeException the runtime exception
 */

@Override
public Object visit(Literal expression, Object context)
        throws RuntimeException {
    try {
        // evaluate the expression
        Object literal = evaluateLiteral(expression,
                (context instanceof Class<?> ? (Class<?>) context : null));

        if (literal instanceof Geometry) {
            visitLiteralGeometry(filterFactory.literal(literal));
            return context;
        }
    } catch (IOException e) {
        throw new RuntimeException("IO problems writing literal", e);
    }

    return super.visit(expression, context);
}

/**
 * Encode: {Expression}.{SA Spatial Function}( {Expression} ) = 1
 *
 * @param filter the filter
 * @param e1 the e1
 * @param e2 the e2
 * @param swapped the swapped
 * @param extraData the extra data
 * @return the object
 */

private Object visitBinarySpatialOperator(BinarySpatialOperator filter,
        Expression e1, Expression e2, boolean swapped, Object extraData) {
    try {
        LOGGER.finer("Generating GeometryFilter WHERE clause for " + filter);

        resetGeomLiteralValidity();
        if (swapped) {
            e2.accept(this, extraData);
        } else {
            e1.accept(this, extraData);
        }
        if (!isGeomLiteralValid) {
            out.write("1");
            return extraData;
        }

        if (filter instanceof Equals) {
            out.write(".ST_Equals");
        } else if (filter instanceof Disjoint) {
            out.write(".ST_Disjoint");
        } else if (filter instanceof Intersects) {
            out.write(".ST_Intersects");
        } else if (filter instanceof BBOX) {
            out.write(".ST_Intersects");
        } else if (filter instanceof Crosses) {
            out.write(".ST_Crosses");
        } else if (filter instanceof Within) {
            out.write(".ST_Within");
        } else if (filter instanceof Contains) {
            out.write(".ST_Covers");
        } else if (filter instanceof Overlaps) {
            out.write(".ST_Overlaps");
        } else if (filter instanceof Touches) {
            out.write(".ST_Touches");
        } else {
            throw new RuntimeException(
                    "Unsupported filter type " + filter.getClass());
        }

        out.write("(");

        resetGeomLiteralValidity();
        if (swapped) {
            e1.accept(this, extraData);
        } else {
            e2.accept(this, extraData);
        }
        if (!isGeomLiteralValid) {
            if (swapped) {
                e2.accept(this, extraData);
            } else {
                e1.accept(this, extraData);
            }
        }

        out.write(") = 1 ");

        LOGGER.fine(this.out.toString());
        return extraData;
    } catch (IOException ex) {
        throw new RuntimeException(ex);
    }
}

/**
 * Parse: {Expression} {Operation} {Expression}.
 *
 * @param filter the filter
 * @param e1 the e1
 * @param e2 the e2
 * @param extraData the extra data
 * @return the object
 */

@Override
protected Object visitBinarySpatialOperator(BinarySpatialOperator filter,
        Expression e1, Expression e2, Object extraData) {
    return visitBinarySpatialOperator(filter, e1, e2, false, extraData);
}

/**
 * Parse: {Expression} {Operation} {Expression}.
 *
 * @param filter the filter
 * @param property the property
 * @param geometry the geometry
 * @param swapped the swapped
 * @param extraData the extra data
 * @return the object
 */

@Override
protected Object visitBinarySpatialOperator(BinarySpatialOperator filter,
        PropertyName property, Literal geometry, boolean swapped,
        Object extraData) {
    // remove unsupported filter for round-earth
    boolean isRoundEarth = ((Boolean) featureType.getUserData()
            .get(HanaDialect.SRS_IS_ROUND_EARTH)).booleanValue();
    if (isRoundEarth) {
        if (filter instanceof Overlaps || filter instanceof Touches
                || filter instanceof Within || filter instanceof Crosses
                || filter instanceof Contains
                || filter instanceof DistanceBufferOperator) {
            try {
                out.write("1");
                return extraData;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    // handle simple binary and distance operations separately.
    if (filter instanceof DistanceBufferOperator)
        return visitDistanceSpatialOperator((DistanceBufferOperator) filter,
                property, geometry, swapped, extraData);
    else
        return visitBinarySpatialOperator(filter, (Expression) property,
                (Expression) geometry, swapped, extraData);
}

// The following methods have not yet been tested

/**
 * Parse: {Expression}.ST_Distance( {Expression} ) <> distance
 *
 * @param filter the filter
 * @param property the property
 * @param geometry the geometry
 * @param swapped the swapped
 * @param extraData the extra data
 * @return the object
 */

private Object visitDistanceSpatialOperator(DistanceBufferOperator filter,
        PropertyName property, Literal geometry, boolean swapped,
        Object extraData) {
    try {
        geometry.accept(this, extraData);
        out.write(".ST_WithinDistance(");
        property.accept(this, extraData);
        out.write(", ");
        out.write(Double.toString(filter.getDistance()));

        if (filter.getDistanceUnits() != null
                && filter.getDistanceUnits().trim() != "") {
            out.write(", '");
            out.write(unitsOfMeasureMapping(filter.getDistanceUnits()));
            out.write("'");
        }

        if ((filter instanceof DWithin && !swapped)
                || (filter instanceof Beyond && swapped)) {
            out.write(") = 1 ");
        } else if ((filter instanceof DWithin && swapped)
                || (filter instanceof Beyond && !swapped)) {
            out.write(") = 0 ");
        }

        return extraData;

    } catch (IOException ex) {
        throw new RuntimeException(ex);
    }
}

// The following three methods are unchecked. Not sure when and how is it
// working for Round Earth

/**
 * Encode: {Function}( {Expression}, {Expression} ).
 *
 * @param function the function
 * @param extraData the extra data
 * @return true, if successful
 * @throws IOException Signals that an I/O exception has occurred.
 */

public boolean visitFunction(Function function, Object extraData)
        throws IOException {
    if (function instanceof FilterFunction_strConcat) {
        Expression s1 = getParameter(function, 0, true);
        Expression s2 = getParameter(function, 1, true);

        out.write("(");
        s1.accept(this, String.class);
        out.write(" || ");
        s2.accept(this, String.class);
        out.write(")");

    } else if (function instanceof FilterFunction_strEndsWith) {
        Expression str = getParameter(function, 0, true);
        Expression end = getParameter(function, 1, true);

        out.write("IF (");
        str.accept(this, String.class);
        out.write(" LIKE ");
        if (end instanceof Literal) {
            out.write("'%" + end.evaluate(null, String.class) + "'");
        } else {
            out.write("('%' || ");
            end.accept(this, String.class);
            out.write(")");
        }
        out.write(") THEN 1 ELSE 0 ENDIF");

    } else if (function instanceof FilterFunction_strStartsWith) {
        Expression str = getParameter(function, 0, true);
        Expression start = getParameter(function, 1, true);

        out.write("IF (");
        str.accept(this, String.class);
        out.write(" LIKE ");
        if (start instanceof Literal) {
            out.write("'" + start.evaluate(null, String.class) + "%'");
        } else {
            out.write("(");
            start.accept(this, String.class);
            out.write(" || '%')");
        }
        out.write(") THEN 1 ELSE 0 ENDIF");

    } else if (function instanceof FilterFunction_strEqualsIgnoreCase) {
        Expression first = getParameter(function, 0, true);
        Expression second = getParameter(function, 1, true);

        out.write("IF ( lower(");
        first.accept(this, String.class);
        out.write(") = lower(");
        second.accept(this, String.class);
        out.write(") ) THEN 1 ELSE 0 ENDIF");

    } else if (function instanceof FilterFunction_strIndexOf) {
        Expression first = getParameter(function, 0, true);
        Expression second = getParameter(function, 1, true);

        // would be a simple call, but strIndexOf returns zero based indices
        out.write("(locate(");
        first.accept(this, String.class);
        out.write(", ");
        second.accept(this, String.class);
        out.write(") - 1)");

    } else if (function instanceof FilterFunction_strSubstring) {
        Expression string = getParameter(function, 0, true);
        Expression start = getParameter(function, 1, true);
        Expression end = getParameter(function, 2, true);

        out.write("substr(");
        string.accept(this, String.class);
        out.write(", ");
        start.accept(this, Integer.class);
        out.write(" + 1, (");
        end.accept(this, Integer.class);
        out.write(" - ");
        start.accept(this, Integer.class);
        out.write("))");

    } else if (function instanceof FilterFunction_strSubstringStart) {
        Expression string = getParameter(function, 0, true);
        Expression start = getParameter(function, 1, true);

        out.write("substr(");
        string.accept(this, String.class);
        out.write(", ");
        start.accept(this, Integer.class);
        out.write(" + 1)");

    } else if (function instanceof FilterFunction_strTrim) {
        Expression string = getParameter(function, 0, true);

        out.write("trim(");
        string.accept(this, String.class);
        out.write(")");

    } else if (function instanceof FilterFunction_strLength) {
        Expression string = getParameter(function, 0, true);

        out.write("length(");
        string.accept(this, String.class);
        out.write(")");

    } else if (function instanceof FilterFunction_strToLowerCase) {
        Expression string = getParameter(function, 0, true);

        out.write("lower(");
        string.accept(this, String.class);
        out.write(")");

    } else if (function instanceof FilterFunction_strToUpperCase) {
        Expression string = getParameter(function, 0, true);

        out.write("upper(");
        string.accept(this, String.class);
        out.write(")");

    } else if (function instanceof FilterFunction_abs
            || function instanceof FilterFunction_abs_2
            || function instanceof FilterFunction_abs_3
            || function instanceof FilterFunction_abs_4) {
        Expression string = getParameter(function, 0, true);

        out.write("CAST (");
        out.write("abs(");
        string.accept(this, String.class);
        out.write(")");
        out.write(" AS ");
        String HanaType = null;
        if (function instanceof FilterFunction_abs)
            HanaType = "SMALLINT";
        if (function instanceof FilterFunction_abs_2)
            HanaType = "INT";
        if (function instanceof FilterFunction_abs_3)
            HanaType = "FLOAT";
        if (function instanceof FilterFunction_abs_4)
            HanaType = "DOUBLE";
        out.write(HanaType);
        out.write(")");

    } else {
        // function not supported
        return false;
    }

    return true;
}

/**
 * Encode: {Geometry Literal}.
 *
 * @param expression the expression
 * @throws IOException Signals that an I/O exception has occurred.
 */

@Override
public void visitLiteralGeometry(Literal expression) throws IOException {
    StringBuffer geomWKT = new StringBuffer();
    Geometry g = (Geometry) evaluateLiteral(expression, Geometry.class);
    if (g instanceof LinearRing) {
        // In SAP HANA, a linear ring is an ST_LineString value which is
        // closed and simple.
        g = g.getFactory()
                .createLineString(((LinearRing) g).getCoordinateSequence());
    }

    // Truncate the literal to less than or equal to the size of the SRS
    // extent
    Envelope bound = g.getEnvelopeInternal();
    double min_x = bound.getMinX();
    double max_x = bound.getMaxX();
    double min_y = bound.getMinY();
    double max_y = bound.getMaxY();

    double srs_min_x = ((Double) featureType.getUserData()
            .get(HanaDialect.SRS_MIN_X)).doubleValue();
    double srs_max_x = ((Double) featureType.getUserData()
            .get(HanaDialect.SRS_MAX_X)).doubleValue();
    double srs_min_y = ((Double) featureType.getUserData()
            .get(HanaDialect.SRS_MIN_Y)).doubleValue();
    double srs_max_y = ((Double) featureType.getUserData()
            .get(HanaDialect.SRS_MAX_Y)).doubleValue();

    if (min_x < srs_min_x || max_x > srs_max_x || min_y < srs_min_y
            || max_y > srs_max_y) {
        min_x = Math.max(min_x, srs_min_x);
        max_x = Math.min(max_x, srs_max_x);
        min_y = Math.max(min_y, srs_min_y);
        max_y = Math.min(max_y, srs_max_y);
        geomWKT.append("ST_GeomFromText('");

        if (min_x == max_x && min_y == max_y) {
            geomWKT.append("Point(" + min_x + " " + min_y + ")");
        } else {
            geomWKT.append("Polygon((");
            geomWKT.append(min_x + " " + min_y + ",");
            geomWKT.append(max_x + " " + min_y + ",");
            geomWKT.append(max_x + " " + max_y + ",");
            geomWKT.append(min_x + " " + max_y + ",");
            geomWKT.append(min_x + " " + min_y + "))");
        }
        geomWKT.append("', " + getSRID() + ")");
    } else {
        geomWKT.append(
                "ST_GeomFromText('" + g.toText() + "', " + getSRID() + ")");
    }

    // if round-earth data, validate geometry against SAP HANA
    boolean isRoundEarth = ((Boolean) featureType.getUserData()
            .get(HanaDialect.SRS_IS_ROUND_EARTH)).booleanValue();

    if (isRoundEarth && !testCreateGeometry(geomWKT.toString(), min_x, max_x))
        isGeomLiteralValid = false;
    else
        out.write(geomWKT.toString());
}

/**
 * Encode: {Date Literal}.
 *
 * @param literal the literal
 * @throws IOException Signals that an I/O exception has occurred.
 */

@Override
protected void writeLiteral(Object literal) throws IOException {
    if (literal instanceof Date) {
        out.write("'");
        if (literal instanceof java.sql.Date) {
            out.write(DATE_FORMAT.format(literal));
        } else {
            out.write(DATETIME_FORMAT.format(literal));
        }
        out.write("'");
    } else {
        super.writeLiteral(literal);
    }
}
}
