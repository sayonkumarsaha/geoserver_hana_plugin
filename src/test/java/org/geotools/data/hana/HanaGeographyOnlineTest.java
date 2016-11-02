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

import org.geotools.jdbc.JDBCGeographyOnlineTest;
import org.geotools.jdbc.JDBCGeographyTestSetup;

/**
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */
public class HanaGeographyOnlineTest extends JDBCGeographyOnlineTest {

@Override
protected JDBCGeographyTestSetup createTestSetup() {
    return new HanaGeographyTestSetup(new HanaTestSetup());
}

@Override
public void testDistanceMeters() throws Exception {
    // Disabled because distance capabilities are not
    // supported by Hana for round-earth data
}

@Override
public void testDistanceGreatCircle() throws Exception {
    // Disabled because distance capabilities are not
    // supported by Hana for round-earth data
}
}