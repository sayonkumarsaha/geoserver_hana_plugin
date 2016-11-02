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
import java.util.Map;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.SQLDialect;

/**
 * DataStore Factory for SAP HANA. Abstract implementation of DataStoreFactory
 * for jdbc datastores.
 *
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */

public class HanaDataStoreFactory extends JDBCDataStoreFactory {

/**
 * Parameters for Database type (default entry HANA), Port number (default entry
 * 30015), and Schema (default entry public). Port number should be of the
 * format 3<instance number>15. Database parameter not needed for HANA. Tables
 * are contained directly under Schema.
 */

public static final Param DBTYPE = new Param("dbtype", String.class, "Type",
        true, "HANA");

public static final Param PORT = new Param("port", Integer.class, "Type", true,
        30015);

/**
 * @return Dialect created that the datastore uses for communication with the
 *         underlying database.
 */

@Override
protected SQLDialect createSQLDialect(JDBCDataStore dataStore) {
    return new HanaDialect(dataStore);
}

/**
 * @return String to identify the type of the database.
 */
@Override
protected String getDatabaseID() {
    return (String) DBTYPE.sample;
}

/**
 * @return Nature of the data-source constructed by this factory.
 */

@Override
public String getDescription() {
    return "SAP HANA Database";
}

/**
 * @return Name suitable for display to end user.
 */

@Override
public String getDisplayName() {
    return "SAP HANA";
}

/**
 * @return fully qualified class name of the jdbc driver.
 */

@Override
protected String getDriverClassName() {
    return "com.sap.db.jdbc.Driver";
}

/**
 * Builds up the JDBC url in the following way- "jdbc:sap://<server>:<port>"
 */

@SuppressWarnings("unchecked")
@Override
protected String getJDBCUrl(Map params) throws IOException {
    String host = (String) HOST.lookUp(params);
    Integer port = (Integer) PORT.lookUp(params);
    String url = "jdbc:sap://" + host + ":" + port;
    return url;
}

/**
 * @return quick validation query, or return null if the factory does not
 *         support validation.
 */

@Override
protected String getValidationQuery() {
    return "SELECT NOW() FROM DUMMY";
}

/**
 * Sets up the database connection parameters.
 */

@SuppressWarnings("unchecked")
@Override
protected void setupParameters(Map parameters) {
    super.setupParameters(parameters);
    parameters.put(HanaDataStoreFactory.DBTYPE.key, DBTYPE);
    parameters.put(HanaDataStoreFactory.PORT.key, PORT);
}
}
