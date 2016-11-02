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

import org.geotools.jdbc.JDBCGeographyTestSetup;
import org.geotools.jdbc.JDBCTestSetup;

/**
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */
public class HanaGeographyTestSetup extends JDBCGeographyTestSetup {

public HanaGeographyTestSetup(JDBCTestSetup delegate) {
    super(delegate);
}

@Override
protected void createGeoPointTable() throws Exception {
    run("CREATE COLUMN TABLE \"GeoToolsTest\".\"geopoint\" "
            + "(\"id\" INTEGER," + "\"geo\" ST_POINT(4326),"
            + " \"name\" VARCHAR(64)," + "PRIMARY KEY (\"id\"));");
    run("INSERT INTO \"GeoToolsTest\".\"geopoint\""
            + "(\"id\", \"name\", \"geo\") "
            + "VALUES (0, 'Town', NEW ST_POINT('POINT(-110 30)'));");
    run("INSERT INTO \"GeoToolsTest\".\"geopoint\""
            + "(\"id\", \"name\", \"geo\") "
            + "VALUES (1, 'Forest', NEW ST_POINT('POINT(-109 29)'));");
    run("INSERT INTO \"GeoToolsTest\".\""
            + "geopoint\"(\"id\", \"name\", \"geo\") "
            + "VALUES (2, 'London', NEW ST_POINT('POINT(0 49)'));");
}

@Override
protected void dropGeoPointTable() throws Exception {
    runSafe("DROP TABLE \"GeoToolsTest\".\"geopoint\";");
}

@Override
protected void createGeoLineTable() throws Exception {
}

@Override
protected void dropGeoLineTable() throws Exception {
}
}
