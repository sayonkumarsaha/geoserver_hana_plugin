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

import org.geotools.jdbc.JDBCPrimaryKeyFinderOnlineTest;
import org.geotools.jdbc.JDBCPrimaryKeyFinderTestSetup;

/**
 * @author Tony Na, Sybase
 * @source $URL:$
 */
public class HanaPrimaryKeyFinderOnlineTest
        extends JDBCPrimaryKeyFinderOnlineTest {

@Override
protected JDBCPrimaryKeyFinderTestSetup createTestSetup() {
    return new HanaPrimaryKeyFinderTestSetup();
}

@Override
public void testSequencedPrimaryKey() throws Exception {
    // Hana does not do explicit sequences
}
}