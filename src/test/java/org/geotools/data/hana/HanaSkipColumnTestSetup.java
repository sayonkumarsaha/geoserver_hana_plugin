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

import org.geotools.jdbc.JDBCSkipColumnTestSetup;

/**
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */
public class HanaSkipColumnTestSetup extends JDBCSkipColumnTestSetup {

protected HanaSkipColumnTestSetup() {
    super(new HanaTestSetup());
}

@Override
protected void createSkipColumnTable() throws Exception {

    run("CREATE COLUMN TABLE \"GeoToolsTest\".\"skipcolumn\" "
            + "(\"fid\" INTEGER, " + "\"id\" INTEGER, "
            + "\"geom\" ST_POINT(1000004326), " + "\"weirdproperty\" INTEGER, "
            + "\"name\" VARCHAR(255), " + "PRIMARY KEY (\"fid\"));");
    run("INSERT INTO \"GeoToolsTest\".\"skipcolumn\" "
            + "(\"fid\",\"id\",\"geom\",\"weirdproperty\",\"name\") "
            + "VALUES (0, 0,NEW ST_POINT('POINT(0 0)'), null, 'GeoTools');");
}

@Override
protected void dropSkipColumnTable() throws Exception {
    runSafe("DROP TABLE \"GeoToolsTest\".\"skipcolumn\";");
}
}