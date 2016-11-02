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

import org.geotools.jdbc.JDBCEmptyGeometryTestSetup;

/**
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */
public class HanaEmptyGeometryTestSetup extends JDBCEmptyGeometryTestSetup {

public HanaEmptyGeometryTestSetup() {
    super(new HanaTestSetup());
}

@Override
protected void createEmptyGeometryTable() throws Exception {
    run("CREATE COLUMN TABLE \"GeoToolsTest\".\"empty\" " + "(\"fid\" INTEGER, "
            + "\"id\" INTEGER, " + "\"geom_point\" ST_Point, "
            + "\"geom_linestring\" ST_Geometry, "
            + "\"geom_polygon\" ST_Geometry, "
            + "\"geom_multipoint\" ST_Geometry, "
            + "\"geom_multilinestring\" ST_Geometry, "
            + "\"geom_multipolygon\" ST_Geometry, " + "\"name\" VARCHAR(255), "
            + "PRIMARY KEY (\"fid\"));");
}

@Override
protected void dropEmptyGeometryTable() throws Exception {
    run("DROP TABLE \"GeoToolsTest\".\"empty\"");
}
}
