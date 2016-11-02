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

import org.geotools.jdbc.JDBCNoPrimaryKeyTestSetup;

/**
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */
public class HanaNoPrimaryKeyTestSetup extends JDBCNoPrimaryKeyTestSetup {

protected HanaNoPrimaryKeyTestSetup() {
    super(new HanaTestSetup());
}

protected void createLakeTable() throws Exception {

    run("CREATE COLUMN TABLE \"GeoToolsTest\".\"lake\""
            + "(\"id\" INTEGER, \"geom\" ST_GEOMETRY(1000004326), \"name\" VARCHAR(255));");
    run("INSERT INTO \"GeoToolsTest\".\"lake\" (\"id\", \"geom\", \"name\") "
            + "VALUES (1, NEW ST_POLYGON('POLYGON((12 6, 14 8, 16 6, 16 4, 14 4, 12 6))'), 'muddy');");
}

@Override
protected void dropLakeTable() throws Exception {
    runSafe("DROP TABLE \"GeoToolsTest\".\"lake\";");
}
}
