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

package com.alesharik.webserver.api.serial;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SerializationClassConversionMapImplTest {
    private SerializationClassConversionMapImpl map;

    @Before
    public void setUp() {
        map = new SerializationClassConversionMapImpl();
    }

    @Test
    public void addConversionSimple() {
        assertNull(map.resolveConversion(1));
        map.addConversion(1, Object.class);
        assertEquals(Object.class, map.resolveConversion(1));
    }

    @Test(expected = IllegalStateException.class)
    public void addConversionWithSameIds() {
        assertNull(map.resolveConversion(1));
        map.addConversion(1, Object.class);
        map.addConversion(1, String.class);
    }

    @Test
    public void addConversionWithAutoId() {
        assertNull(map.resolveConversion(0));
        assertEquals(0, map.addConversion(Object.class));
        assertEquals(Object.class, map.resolveConversion(0));
    }

    @Test
    public void addConversionsWithAutoId() {
        for(int i = 0; i < 1000; i++) {
            map.addConversion(Object.class);
        }
    }

    @Test
    public void removeConversionsForClassLoader() {
        assertNull(map.resolveConversion(1));
        map.addConversion(1, Object.class);
        assertEquals(Object.class, map.resolveConversion(1));
        map.cleanClassesFromClassLoader(Object.class.getClassLoader());
        assertNull(map.resolveConversion(1));
    }

    @Test
    public void getNextId() {
        assertEquals(0, map.getNextId());
        map.addConversion(Object.class);
        assertEquals(1, map.getNextId());
        map.addConversion(100, String.class);
        assertEquals(101, map.getNextId());
    }

    @Test
    public void resolveConversion() {
        long id = map.addConversion(Object.class);
        assertEquals(Object.class, map.resolveConversion(id));
        assertNull(map.resolveConversion(id + 1));
    }

    @Test
    public void getConversionForId() {
        assertEquals(-1, map.getConversionFor(Object.class));
        long l = map.addConversion(Object.class);
        assertEquals(l, map.getConversionFor(Object.class));
        map.cleanClassesFromClassLoader(Object.class.getClassLoader());
        assertEquals(-1, map.getConversionFor(Object.class));
    }
}