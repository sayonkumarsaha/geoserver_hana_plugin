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

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.geotools.jdbc.JDBCLobTestSetup;

/**
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */
public class HanaLobTestSetup extends JDBCLobTestSetup {

protected HanaLobTestSetup() {
    super(new HanaTestSetup());
}

@Override
protected void createLobTable() throws Exception {

    Connection con = getDataSource().getConnection();
    con.prepareStatement("CREATE COLUMN TABLE \"GeoToolsTest\".\"testlob\" "
            + "(\"fid\" INTEGER," + "\"blob_field\" BLOB,"
            + "\"clob_field\" TEXT," + "\"raw_field\" CLOB,"
            + "PRIMARY KEY (\"fid\"));").execute();

    PreparedStatement ps = con
            .prepareStatement("INSERT INTO \"GeoToolsTest\".\"testlob\"  "
                    + "(\"fid\",\"blob_field\",\"clob_field\",\"raw_field\") "
                    + "VALUES (?,?,?,?)");

    ps.setInt(1, 0);
    ps.setBytes(2, new byte[] { 1, 2, 3, 4, 5 });
    ps.setString(3, "\"small clob\"");
    ps.setBytes(4, new byte[] { 6, 7, 8, 9, 10 });
    ps.execute();
    ps.close();
    con.close();
}

@Override
protected void dropLobTable() throws Exception {
    runSafe("DROP TABLE \"GeoToolsTest\".\"testlob\";");
}
}
