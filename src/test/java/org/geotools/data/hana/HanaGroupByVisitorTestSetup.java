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

import org.geotools.jdbc.JDBCGroupByVisitorTestSetup;

/**
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */
public class HanaGroupByVisitorTestSetup extends JDBCGroupByVisitorTestSetup {

public HanaGroupByVisitorTestSetup() {
    super(new HanaTestSetup());
}

@Override
protected void createBuildingsTable() throws Exception {
    run("CREATE COLUMN TABLE \"GeoToolsTest\".\"buildings_group_by_tests\" "
            + "(\"id\" INTEGER, \"building_id\" TEXT, \"building_type\" TEXT,"
            + "\"energy_type\" TEXT, \"energy_consumption\" NUMERIC, PRIMARY KEY (\"id\"));");
    run("INSERT INTO \"buildings_group_by_tests\" (\"id\", \"building_id\", \"building_type\", "
            + "\"energy_type\", \"energy_consumption\") VALUES (1, 'SCHOOL_A', 'SCHOOL', 'FLOWING_WATER', 50.0);");
    run("INSERT INTO \"buildings_group_by_tests\" (\"id\", \"building_id\", \"building_type\", "
            + "\"energy_type\", \"energy_consumption\") VALUES (2, 'SCHOOL_A', 'SCHOOL', 'NUCLEAR', 10.0);");
    run("INSERT INTO \"buildings_group_by_tests\" (\"id\", \"building_id\", \"building_type\", "
            + "\"energy_type\", \"energy_consumption\") VALUES (3, 'SCHOOL_A', 'SCHOOL', 'WIND', 20.0);");
    run("INSERT INTO \"buildings_group_by_tests\" (\"id\", \"building_id\", \"building_type\", "
            + "\"energy_type\", \"energy_consumption\") VALUES (4, 'SCHOOL_B', 'SCHOOL', 'SOLAR', 30.0);");
    run("INSERT INTO \"buildings_group_by_tests\" (\"id\", \"building_id\", \"building_type\", "
            + "\"energy_type\", \"energy_consumption\") VALUES (5, 'SCHOOL_B', 'SCHOOL', 'FUEL', 60.0);");
    run("INSERT INTO \"buildings_group_by_tests\" (\"id\", \"building_id\", \"building_type\", "
            + "\"energy_type\", \"energy_consumption\") VALUES (6, 'SCHOOL_B', 'SCHOOL', 'NUCLEAR', 10.0);");
    run("INSERT INTO \"buildings_group_by_tests\" (\"id\", \"building_id\", \"building_type\", "
            + "\"energy_type\", \"energy_consumption\") VALUES (7, 'FABRIC_A', 'FABRIC', 'FLOWING_WATER', 500.0);");
    run("INSERT INTO \"buildings_group_by_tests\" (\"id\", \"building_id\", \"building_type\", "
            + "\"energy_type\", \"energy_consumption\") VALUES (8, 'FABRIC_A', 'FABRIC', 'NUCLEAR', 150.0);");
    run("INSERT INTO \"buildings_group_by_tests\" (\"id\", \"building_id\", \"building_type\", "
            + "\"energy_type\", \"energy_consumption\") VALUES (9, 'FABRIC_B', 'FABRIC', 'WIND', 20.0);");
    run("INSERT INTO \"buildings_group_by_tests\" (\"id\", \"building_id\", \"building_type\", "
            + "\"energy_type\", \"energy_consumption\") VALUES (10, 'FABRIC_B', 'FABRIC', 'SOLAR', 30.0);");
    run("INSERT INTO \"buildings_group_by_tests\" (\"id\", \"building_id\", \"building_type\", "
            + "\"energy_type\", \"energy_consumption\") VALUES (11, 'HOUSE_A', 'HOUSE', 'FUEL', 6.0);");
    run("INSERT INTO \"buildings_group_by_tests\" (\"id\", \"building_id\", \"building_type\", "
            + "\"energy_type\", \"energy_consumption\") VALUES (12, 'HOUSE_B', 'HOUSE', 'NUCLEAR', 4.0);");
}

@Override
protected void dropBuildingsTable() throws Exception {
    runSafe("DROP TABLE \"GeoToolsTest\".\"buildings_group_by_tests\";");
}
}