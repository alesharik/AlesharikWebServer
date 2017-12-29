/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.api.cache.object;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FixedCachedObjectFactoryTest {
    private FixedCachedObjectFactory<TestClass> factory;

    @Before
    public void setUp() throws Exception {
        factory = new FixedCachedObjectFactory<>(16, TestClass::new);
    }

    @Test
    public void testCreateObjectHeap() throws Exception {
        for(int i = 0; i < 1000; i++) {
            assertNotNull(factory.getInstance());
        }
    }

    @Test
    public void testCacheWorks() throws Exception {
        TestClass[] cacheDump = new TestClass[16];
        for(int i = 0; i < 16; i++) {
            cacheDump[i] = factory.getInstance();
        }

        for(TestClass testClass : cacheDump) {
            factory.putInstance(testClass);
        }

        for(int i = 0; i < 16; i++) {
            assertEquals(cacheDump[i], factory.getInstance());
        }
    }

    @Test
    public void testMxBeanOptions() throws Exception {
        assertEquals(0, factory.getMinCachedObjectCount());
        assertEquals(16, factory.getMaxCachedObjectCount());

        assertEquals(16, factory.getCurrentCachedObjectCount());

        TestClass[] dump = new TestClass[8];
        for(int i = 0; i < 8; i++) {
            dump[i] = factory.getInstance();
        }

        assertEquals(8, factory.getCurrentCachedObjectCount());

        for(TestClass testClass : dump) {
            factory.putInstance(testClass);
        }

        assertEquals(16, factory.getCurrentCachedObjectCount());
    }

    private static final class TestClass implements Recyclable {

        @Override
        public void recycle() {
            //Ok
        }
    }
}