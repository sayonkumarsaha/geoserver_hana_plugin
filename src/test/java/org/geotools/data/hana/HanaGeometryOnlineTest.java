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

import org.geotools.jdbc.JDBCGeometryOnlineTest;
import org.geotools.jdbc.JDBCGeometryTestSetup;

/**
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */
public class HanaGeometryOnlineTest extends JDBCGeometryOnlineTest {

@Override
protected JDBCGeometryTestSetup createTestSetup() {
    return new HanaGeometryTestSetup();
}

// Instead of modifying the test case, extend the corresponding code in
// GeoTools project

// Modify the test behavior because in Hana all geometries like ST_Point,
// ST_LineString, ST_Polygon, etc are subtype of ST_Geometry.
// For example, ST_Polygon values can call methods from the ST_Geometry.

// public void testLinearRing() throws Exception {
// assertEquals(Geometry.class, checkGeometryType(LinearRing.class));
// }
//
// public void testGeometryCollection() throws Exception {
// assertEquals(Geometry.class,
// checkGeometryType(GeometryCollection.class));
// }
//
// public void testMultiPolygon() throws Exception {
// assertEquals(Geometry.class, checkGeometryType(MultiPolygon.class));
// }
//
// public void testLineString() throws Exception {
// assertEquals(Geometry.class, checkGeometryType(LineString.class));
// }
//
// public void testMultiLineString() throws Exception {
// assertEquals(Geometry.class, checkGeometryType(MultiLineString.class));
// }
//
// public void testPolygon() throws Exception {
// assertEquals(Geometry.class, checkGeometryType(Polygon.class));
// }
//
// public void testPoint() throws Exception {
// assertEquals(Geometry.class, checkGeometryType(Point.class));
// }
//
// public void testMultiPoint() throws Exception {
// assertEquals(Geometry.class, checkGeometryType(MultiPoint.class));
// }
}