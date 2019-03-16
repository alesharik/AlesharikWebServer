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
import static org.mockito.Mockito.*;

public class SmartCachedObjectFactoryTest {
    private SmartCachedObjectFactory<Recyclable> factory;

    @Before
    public void setUp() {
        factory = new SmartCachedObjectFactory<>(() -> mock(Recyclable.class));
    }

    @Test
    public void testSupplyBigHeap() {
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
    }

    @Test
    public void testMxBeanOptions() {
        assertEquals(32, factory.getMinCachedObjectCount());
    }

    @Test
    public void testBasicConcurrency() throws Exception {
        Thread a = new Thread(() -> {
            for(int i = 0; i < 100; i++) {
                Recyclable instance = factory.getInstance();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                factory.putInstance(instance);
            }
        });
        Thread b = new Thread(() -> {
            for(int i = 0; i < 100; i++) {
                Recyclable instance = factory.getInstance();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                factory.putInstance(instance);
            }
        });
        a.start();
        b.start();
        a.join();
        b.join();
    }
}