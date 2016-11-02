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

import static org.geotools.jdbc.JDBCDataStoreFactory.DBTYPE;
import static org.geotools.jdbc.JDBCDataStoreFactory.HOST;
import static org.geotools.jdbc.JDBCDataStoreFactory.PASSWD;
import static org.geotools.jdbc.JDBCDataStoreFactory.PORT;
//import static org.geotools.jdbc.JDBCDataStoreFactory.DATABASE;
import static org.geotools.jdbc.JDBCDataStoreFactory.SCHEMA;
import static org.geotools.jdbc.JDBCDataStoreFactory.USER;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCTestSetup;
import org.geotools.jdbc.JDBCTestSupport;

/**
 * @author Sayon Kumar Saha, SAP
 * @source $URL:$
 */
public class HanaDataStoreFactoryTest extends JDBCTestSupport {

@Override
protected JDBCTestSetup createTestSetup() {
    return new HanaTestSetup();
}

public void testCreateDataStoreWithDatabase() throws Exception {
    checkConnection();
}

void checkConnection() throws Exception {
    Properties db = fixture;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put(HOST.key, db.getProperty(HOST.key));
    params.put(SCHEMA.key, db.getProperty(SCHEMA.key));
    params.put(PORT.key, db.getProperty(PORT.key));
    params.put(USER.key, db.getProperty(USER.key));
    params.put(PASSWD.key, db.getProperty(PASSWD.key));

    HanaDataStoreFactory factory = new HanaDataStoreFactory();
    params.put(DBTYPE.key, factory.getDatabaseID());

    assertTrue(factory.canProcess(params));

    JDBCDataStore store = factory.createDataStore(params);
    assertNotNull(store);
    try {
        // check dialect
        assertTrue(store.getSQLDialect() instanceof HanaDialect);
    } finally {
        store.dispose();
    }
}
}
