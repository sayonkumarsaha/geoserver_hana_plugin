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

import org.geotools.jdbc.JDBCTypeNamesTestSetup;

/**
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */
public class HanaTypeNamesTestSetup extends JDBCTypeNamesTestSetup {

protected HanaTypeNamesTestSetup() {
    super(new HanaTestSetup());
}

@Override
protected void createTypes() throws Exception {
    run("CREATE COLUMN TABLE \"GeoToolsTest\".\"ftntable\" (\"id\" INTEGER, \"name\" VARCHAR(255), \"geom\" ST_GEOMETRY);");
    run("CREATE VIEW \"GeoToolsTest\".\"ftnview\" AS SELECT \"id\", \"geom\" FROM \"GeoToolsTest\".\"ftntable\";");
}

@Override
protected void dropTypes() throws Exception {
    runSafe("DROP VIEW \"GeoToolsTest\".\"ftnview\"");
    runSafe("DROP TABLE \"GeoToolsTest\".\"ftntable\"");
}
}
