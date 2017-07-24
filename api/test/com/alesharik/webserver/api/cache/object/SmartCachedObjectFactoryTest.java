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

public class SmartCachedObjectFactoryTest {
    private SmartCachedObjectFactory<TestClass> factory;

    @Before
    public void setUp() throws Exception {
        factory = new SmartCachedObjectFactory<>(TestClass::new);
    }

    @Test
    public void testSupplyBigHeap() throws Exception {
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
    }

    @Test
    public void testMxBeanOptions() throws Exception {
        assertEquals(0, factory.getMinCachedObjectCount());
    }


    private static final class TestClass implements Recyclable {

        @Override
        public void recycle() {

        }
    }
}