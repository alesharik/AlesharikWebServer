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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FixedCachedObjectFactoryTest {
    private FixedCachedObjectFactory<Recyclable> factory;

    @Before
    public void setUp() {
        factory = new FixedCachedObjectFactory<>(16, () -> mock(Recyclable.class));
    }

    @Test
    public void testCreateObjectHeap() {
        for(int i = 0; i < 1000; i++)
            assertNotNull(factory.getInstance());
    }

    @Test
    public void testCacheWorks() {
        Recyclable[] cacheDump = new Recyclable[16];
        for(int i = 0; i < 16; i++)
            cacheDump[i] = factory.getInstance();

        for(Recyclable testClass : cacheDump)
            factory.putInstance(testClass);

        for(Recyclable recyclable : cacheDump)
            verify(recyclable, times(1)).recycle();

        for(int i = 0; i < 16; i++)
            assertSame(cacheDump[i], factory.getInstance());
    }

    @Test
    public void testMxBeanOptions() {
        assertEquals(0, factory.getMinCachedObjectCount());
        assertEquals(16, factory.getMaxCachedObjectCount());

        assertEquals(16, factory.getCurrentCachedObjectCount());

        Recyclable[] dump = new Recyclable[8];
        for(int i = 0; i < 8; i++)
            dump[i] = factory.getInstance();

        assertEquals(8, factory.getCurrentCachedObjectCount());

        for(Recyclable testClass : dump)
            factory.putInstance(testClass);

        assertEquals(16, factory.getCurrentCachedObjectCount());
    }
}