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

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCViewTestSetup;

/**
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */
public class HanaViewTestSetup extends JDBCViewTestSetup {

protected HanaViewTestSetup() {
    super(new HanaTestSetup());

}

@Override
protected void setUpDataStore(JDBCDataStore dataStore) {
    super.setUpDataStore(dataStore);

    dataStore.setDatabaseSchema("GeoToolsTest");
}

@Override
protected void createLakesTable() throws Exception {

    run("CREATE COLUMN TABLE \"GeoToolsTest\".\"lake\"" + "(\"fid\" INTEGER, "
            + "\"id\" INTEGER, " + "\"geom\" ST_GEOMETRY(1000004326),"
            + "\"name\" VARCHAR(255)," + "PRIMARY KEY (\"fid\"));");

    run("INSERT INTO \"GeoToolsTest\".\"lake\" "
            + "(\"fid\",\"id\",\"geom\", \"name\") " + "VALUES (1, 0, "
            + "NEW ST_POLYGON('POLYGON((12 6, 14 8, 16 6, 16 4, 14 4, 12 6))'),"
            + "'muddy');");
}

@Override
protected void dropLakesTable() throws Exception {
    runSafe("DROP TABLE \"GeoToolsTest\".\"lake\";");
}

@Override
protected void createLakesView() throws Exception {
    run("CREATE VIEW \"GeoToolsTest\".\"lakesview\" AS SELECT * from \"GeoToolsTest\".\"lake\"");
}

@Override
protected void dropLakesView() throws Exception {
    runSafe("DROP VIEW \"GeoToolsTest\".\"lakesview\";");
}

@Override
protected void createLakesViewPk() throws Exception {

}

@Override
protected void dropLakesViewPk() throws Exception {

}
}
