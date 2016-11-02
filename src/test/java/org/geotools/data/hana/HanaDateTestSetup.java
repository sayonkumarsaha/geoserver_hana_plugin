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

import org.geotools.jdbc.JDBCDateTestSetup;

/**
 * @author Tony Na, Sybase
 * @source $URL:$
 */
public class HanaDateTestSetup extends JDBCDateTestSetup {

protected HanaDateTestSetup() {
    super(new HanaTestSetup());
}

@Override
protected void createDateTable() throws Exception {
    run("CREATE COLUMN TABLE \"GeoToolsTest\".\"dates\" " + "(\"id\" INTEGER,  "
            + "\"d\" DATE, " + "\"dt\" TIMESTAMP, " + "\"t\" TIME, "
            + "\"geom\" ST_GEOMETRY, " + "PRIMARY KEY (\"id\"));");

    run("INSERT INTO \"GeoToolsTest\".\"dates\" "
            + "(\"id\", \"d\",\"dt\",\"t\")" + "VALUES (1, "
            + "CAST('2009-06-28' as DATE), "
            + "CAST('2009-06-28 15:12:41' as TIMESTAMP), "
            + "CAST('15:12:41' as TIME));");

    run("INSERT INTO \"GeoToolsTest\".\"dates\" "
            + "(\"id\", \"d\",\"dt\",\"t\")" + "VALUES (2, "
            + "CAST('2009-01-15' as DATE), "
            + "CAST('2009-01-15 13:10:12' as TIMESTAMP), "
            + "CAST('13:10:12' as TIME));");

    run("INSERT INTO \"GeoToolsTest\".\"dates\" "
            + "(\"id\", \"d\",\"dt\",\"t\")" + "VALUES (3, "
            + "CAST('2009-09-29' as DATE), "
            + "CAST('2009-09-29 17:54:23' as TIMESTAMP), "
            + "CAST('17:54:23' as TIME));");
}

@Override
protected void dropDateTable() throws Exception {
    runSafe("DROP TABLE \"GeoToolsTest\".\"dates\";");
}
}
