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

package com.alesharik.webserver.api.ticking;

import org.junit.Before;
import org.junit.Test;

import static com.alesharik.webserver.api.ticking.ExecutorPoolBasedTickingPool.TickableCache;
import static org.junit.Assert.*;

public class TickableCacheTest {
    public static final Tickable TICKABLE = () -> {
    };
    public static final Tickable TICKABLE1 = () -> {
    };
    private TickableCache tickableCache;
    private TickableCache equals;
    private TickableCache notEquals;

    @Before
    public void setUp() {
        tickableCache = new TickableCache(TICKABLE);
        equals = new TickableCache(TICKABLE);
        notEquals = new TickableCache(TICKABLE1);
    }

    @Test
    public void equalsTest() {
        assertTrue(tickableCache.equals(equals));
        assertFalse(tickableCache.equals(notEquals));
    }

    @Test
    public void hashCodeTest() {
        assertEquals(tickableCache.hashCode(), equals.getHashCode());
        assertFalse(Integer.compare(tickableCache.hashCode(), notEquals.hashCode()) == 0);
    }

    @Test
    public void hashCodeSaveTest() {
        assertEquals(TICKABLE.hashCode(), tickableCache.hashCode());
        assertEquals(TICKABLE.hashCode(), equals.hashCode());
    }
}