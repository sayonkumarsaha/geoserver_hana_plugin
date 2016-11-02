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

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.jdbc.JDBCSkipColumnOnlineTest;
import org.geotools.jdbc.JDBCSkipColumnTestSetup;

/**
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */
public class HanaSkipColumnOnlineTest extends JDBCSkipColumnOnlineTest {

@Override
protected JDBCSkipColumnTestSetup createTestSetup() {
    return new HanaSkipColumnTestSetup();
}

/*
 * Modify the test behavior because Hana's envelop is always a polygon, which is
 * at least 4 grid large.
 */
@Override
public void testGetBounds() throws Exception {
    ReferencedEnvelope env = dataStore.getFeatureSource(tname(SKIPCOLUMN))
            .getBounds();
    assertEquals(-1.7974525690078735E-7, env.getMinX());
    assertEquals(-1.7974525690078735E-7, env.getMinY());
    assertEquals(1.7974525690078735E-7, env.getMaxX());
    assertEquals(1.7974525690078735E-7, env.getMaxY());
}
}