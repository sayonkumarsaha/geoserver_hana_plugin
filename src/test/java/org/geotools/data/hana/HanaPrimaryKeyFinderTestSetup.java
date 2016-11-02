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

import org.geotools.jdbc.JDBCPrimaryKeyFinderTestSetup;

/**
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */
public class HanaPrimaryKeyFinderTestSetup
        extends JDBCPrimaryKeyFinderTestSetup {

public HanaPrimaryKeyFinderTestSetup() {
    super(new HanaTestSetup());
}

@Override
protected void createMetadataTable() throws Exception {
    run("CREATE COLUMN TABLE \"GeoToolsTest\".\"gt_pk_metadata\""
            + "(\"table_schema\" VARCHAR(255), "
            + "\"table_name\" VARCHAR(255), " + "\"pk_column\" VARCHAR(255), "
            + "\"pk_column_idx\" INTEGER,  " + "\"pk_policy\" VARCHAR(255), "
            + "\"pk_sequence\" VARCHAR(255));");
}

@Override
protected void dropMetadataTable() throws Exception {
    runSafe("DROP TABLE \"GeoToolsTest\".\"gt_pk_metadata\";");
}

@Override
protected void createPlainTable() throws Exception {
    run("CREATE COLUMN TABLE \"GeoToolsTest\".\"plaintable\""
            + "(\"key1\" INTEGER, " + "\"key2\" INTEGER, "
            + "\"name\" VARCHAR(255), " + "\"geom\" ST_GEOMETRY(1000004326));");
    run("INSERT INTO \"GeoToolsTest\".\"plaintable\" "
            + "(\"key1\",\"key2\",\"name\",\"geom\") "
            + "VALUES (1, 2, 'one', NULL);");
    run("INSERT INTO \"GeoToolsTest\".\"plaintable\" "
            + "(\"key1\",\"key2\",\"name\",\"geom\") "
            + "VALUES (2, 3, 'two', NULL);");
    run("INSERT INTO \"GeoToolsTest\".\"plaintable\" "
            + "(\"key1\",\"key2\",\"name\",\"geom\") "
            + "VALUES (3, 4, 'three', NULL);");
}

@Override
protected void dropPlainTable() throws Exception {
    runSafe("DROP TABLE \"GeoToolsTest\".\"plaintable\";");
}

@Override
protected void createAssignedSinglePkView() throws Exception {
    run("CREATE VIEW \"GeoToolsTest\".\"assignedsinglepk\" AS SELECT * from \"GeoToolsTest\".\"plaintable\"");
    run("INSERT INTO \"GeoToolsTest\".\"gt_pk_metadata\" "
            + "(\"table_schema\",\"table_name\",\"pk_column\",\"pk_column_idx\",\"pk_policy\",\"pk_sequence\") "
            + "VALUES (NULL, 'assignedsinglepk', 'key1', 0, 'assigned', NULL);");
}

@Override
protected void dropAssignedSinglePkView() throws Exception {
    runSafe("DROP VIEW \"GeoToolsTest\".\"assignedsinglepk\"");
}

@Override
protected void createAssignedMultiPkView() throws Exception {
    run("CREATE VIEW \"GeoToolsTest\".\"assignedmultipk\" AS SELECT * from \"GeoToolsTest\".\"plaintable\"");
    run("INSERT INTO \"GeoToolsTest\".\"gt_pk_metadata\" "
            + "(\"table_schema\",\"table_name\",\"pk_column\",\"pk_column_idx\",\"pk_policy\",\"pk_sequence\") "
            + "VALUES (NULL, 'assignedmultipk', 'key1', 0, 'assigned', NULL);");
    run("INSERT INTO \"GeoToolsTest\".\"gt_pk_metadata\" "
            + "(\"table_schema\",\"table_name\",\"pk_column\",\"pk_column_idx\",\"pk_policy\",\"pk_sequence\") "
            + "VALUES (NULL, 'assignedmultipk', 'key2', 1, 'assigned', NULL);");
}

@Override
protected void dropAssignedMultiPkView() throws Exception {
    runSafe("DROP VIEW \"GeoToolsTest\".\"assignedmultipk\"");
}

@Override
protected void createSequencedPrimaryKeyTable() throws Exception {
    // Hana does not have sequences
}

@Override
protected void dropSequencedPrimaryKeyTable() throws Exception {
}
}