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

import org.geotools.jdbc.JDBCBooleanTestSetup;

/**
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */
public class HanaBooleanTestSetup extends JDBCBooleanTestSetup {

protected HanaBooleanTestSetup() {
    super(new HanaTestSetup());
}

@Override
protected void createBooleanTable() throws Exception {

    run("CREATE COLUMN TABLE \"GeoToolsTest\".\"b\" " + "(\"id\" INTEGER, "
            + "\"boolProperty\" BOOLEAN, " + "\"geom\" ST_GEOMETRY, "
            + "PRIMARY KEY (\"id\"));");

    run("INSERT INTO \"GeoToolsTest\".\"b\" (\"id\", \"boolProperty\") VALUES (1, false);");
    run("INSERT INTO \"GeoToolsTest\".\"b\" (\"id\", \"boolProperty\") VALUES (2, true);");
}

@Override
protected void dropBooleanTable() throws Exception {
    run("DROP TABLE \"GeoToolsTest\".\"b\";");
}
}