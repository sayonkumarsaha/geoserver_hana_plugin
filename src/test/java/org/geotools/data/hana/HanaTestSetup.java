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

import java.util.Properties;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.JDBCTestSetup;

/**
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */
public class HanaTestSetup extends JDBCTestSetup {

@Override
protected JDBCDataStoreFactory createDataStoreFactory() {
    return new HanaDataStoreFactory();
}

@Override
protected Properties createExampleFixture() {
    Properties fixture = new Properties();
    fixture.put("driver", "com.sap.db.jdbc.Driver");
    fixture.put("url", "jdbc:sap://hdbspatial.wdf.sap.corp:30015");
    fixture.put("host", "hdbspatial.wdf.sap.corp");
    fixture.put("port", "30015");
    fixture.put("schema", "GeoToolsTest");
    fixture.put("user", "SYSTEM");
    fixture.put("password", "iiThai5e");
    return fixture;
}

protected void setUpDataStore(JDBCDataStore dataStore) {
    super.setUpDataStore(dataStore);
    dataStore.setDatabaseSchema("GeoToolsTest");
}

protected void setUpData() throws Exception {
    // drop old data
    runSafe("DROP TABLE \"GeoToolsTest\".\"ft1\";");
    runSafe("DROP TABLE \"GeoToolsTest\".\"ft2\";");

    // create the data
    run("CREATE COLUMN TABLE \"GeoToolsTest\".\"ft1\" " + "(\"id\" INTEGER,"
            + "\"geometry\" ST_GEOMETRY(1000004326)," + "\"intProperty\" INT,"
            + "\"doubleProperty\" DOUBLE, " + "\"stringProperty\" VARCHAR(255),"
            + "PRIMARY KEY (\"id\"));");

    run("INSERT INTO \"GeoToolsTest\".\"ft1\" "
            + "(\"id\", \"geometry\", \"intProperty\", \"doubleProperty\", \"stringProperty\") "
            + "VALUES (0, NEW ST_POINT('POINT(0 0)'), 0, 0.0, 'zero');");

    run("INSERT INTO \"GeoToolsTest\".\"ft1\" "
            + "(\"id\", \"geometry\", \"intProperty\", \"doubleProperty\", \"stringProperty\") "
            + "VALUES (1, NEW ST_POINT('POINT(1 1)'), 1, 1.1, 'one');");

    run("INSERT INTO \"GeoToolsTest\".\"ft1\" "
            + "(\"id\", \"geometry\", \"intProperty\", \"doubleProperty\", \"stringProperty\") "
            + "VALUES (2, NEW ST_POINT('POINT(2 2)'), 2, 2.2, 'two');");
}
}
