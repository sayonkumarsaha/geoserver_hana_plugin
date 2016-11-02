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

import org.geotools.jdbc.JDBC3DTestSetup;

/**
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */
public class Hana3DTestSetup extends JDBC3DTestSetup {

protected Hana3DTestSetup() {
    super(new HanaTestSetup());
}

@Override
protected void createLine3DTable() throws Exception {

    // set up table
    run("CREATE COLUMN TABLE \"GeoToolsTest\".\"line3d\" (\"fid\" INTEGER,\"id\" INTEGER,"
            + "\"geom\" ST_GEOMETRY(4326)," + "\"name\" VARCHAR(255),"
            + "PRIMARY KEY (\"fid\"));");
    // insert data
    run("INSERT INTO \"GeoToolsTest\".\"line3d\" (\"fid\",\"id\",\"name\",\"geom\") "
            + "VALUES (0, 0, 'l1',"
            + "NEW ST_LINESTRING('LINESTRING Z(1 1 0, 2 2 0, 4 2 1, 5 1 1)'));");

    run("INSERT INTO \"GeoToolsTest\".\"line3d\" (\"fid\",\"id\",\"name\",\"geom\") "
            + "VALUES (1, 1, 'l2',"
            + "NEW ST_LINESTRING('LINESTRING Z(3 0 1 , 3 2 2 , 3 3 3 , 3 4 5)'));");
}

@Override
protected void createPoint3DTable() throws Exception {
    // set up table
    run("CREATE COLUMN TABLE \"GeoToolsTest\".\"point3d\" (\"fid\" INTEGER,"
            + "\"id\" INTEGER," + "\"geom\" ST_GEOMETRY(4326),"
            + "\"name\" VARCHAR(255)," + "PRIMARY KEY (\"fid\"));");

    // insert data
    run("INSERT INTO \"GeoToolsTest\".\"point3d\" (\"fid\",\"id\",\"name\",\"geom\")  "
            + "VALUES (0, 0, 'p1'," + " NEW ST_POINT('POINT Z(1 1 1)'));");

    run("INSERT INTO \"GeoToolsTest\".\"point3d\" (\"fid\",\"id\",\"name\",\"geom\") "
            + "VALUES (1, 1, 'p2'," + "NEW ST_POINT('POINT Z(3 0 1)'));");
}

@Override
protected void dropLine3DTable() throws Exception {
    runSafe("DROP TABLE  \"GeoToolsTest\".\"line3d\";");
}

@Override
protected void dropPoly3DTable() throws Exception {
    runSafe("DROP TABLE  \"GeoToolsTest\".\"poly3d\";");
}

@Override
protected void dropPoint3DTable() throws Exception {
    runSafe("DROP TABLE  \"GeoToolsTest\".\"point3d\";");
}
}