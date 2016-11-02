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

import org.geotools.jdbc.JDBCJoinTestSetup;

/**
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */
public class HanaJoinTestSetup extends JDBCJoinTestSetup {

protected HanaJoinTestSetup() {
    super(new HanaTestSetup());
}

@Override
protected void createJoinTable() throws Exception {

    run("CREATE COLUMN TABLE \"GeoToolsTest\".\"ftjoin\" "
            + "(\"id\" INTEGER, \"name\" VARCHAR(10), \"geom\" ST_GEOMETRY(1000004326));");
    run("ALTER TABLE \"GeoToolsTest\".\"ftjoin\" "
            + "ALTER (\"name\" VARCHAR(255)); ");
    run("INSERT INTO \"GeoToolsTest\".\"ftjoin\" "
            + "(\"id\", \"name\",\"geom\") " + "VALUES (0, 'zero', "
            + "NEW ST_POLYGON('POLYGON((-0.1 -0.1, -0.1 0.1, 0.1 0.1, 0.1 -0.1, -0.1 -0.1))'));");
    run("INSERT INTO \"GeoToolsTest\".\"ftjoin\" "
            + "(\"id\", \"name\",\"geom\") " + "VALUES (1, 'one', "
            + "NEW ST_POLYGON('POLYGON((-1.1 -1.1, -1.1 1.1, 1.1 1.1, 1.1 -1.1, -1.1 -1.1))'));");
    run("INSERT INTO \"GeoToolsTest\".\"ftjoin\" "
            + "(\"id\", \"name\",\"geom\") " + "VALUES (2, 'two', "
            + "NEW ST_POLYGON('POLYGON((-0.1 -0.1, -0.1 0.1, 0.1 0.1, 0.1 -0.1, -0.1 -0.1))'));");
    run("INSERT INTO \"GeoToolsTest\".\"ftjoin\" "
            + "(\"id\", \"name\",\"geom\") " + "VALUES (3, 'three', NULL);");
}

@Override
protected void dropJoinTable() throws Exception {
    runSafe("DROP TABLE \"GeoToolsTest\".\"ftjoin\"");
}
}
