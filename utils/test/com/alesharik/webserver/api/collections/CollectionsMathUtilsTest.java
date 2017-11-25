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

package com.alesharik.webserver.api.collections;

import org.junit.Test;

import static org.junit.Assert.*;

public class CollectionsMathUtilsTest {
    @Test
    public void getBucketRangeTest() throws Exception {
        for(int i = 0; i < 1000; i++) {
            int bucket = CollectionsMathUtils.getBucket(i * 16, 16);
            assertTrue(bucket > -1);
            assertTrue(bucket < 16);
        }
    }

    @Test
    public void powerOfTwoTest() throws Exception {
        for(int i = 129; i < 255; i++) {
            assertEquals(256, CollectionsMathUtils.powerOfTwoFor(i));
        }
        assertEquals(4, CollectionsMathUtils.powerOfTwoFor(3));
        assertEquals(8, CollectionsMathUtils.powerOfTwoFor(6));
        assertEquals(CollectionsMathUtils.MAP_MAXIMUM_CAPACITY, CollectionsMathUtils.powerOfTwoFor(Integer.MAX_VALUE));

        for(int i = 1; i < -100; i--) {
            assertEquals(1, CollectionsMathUtils.powerOfTwoFor(i));
        }
    }

    @Test
    public void getHashCodeTest() throws Exception {
        assertEquals(0, CollectionsMathUtils.hash(null));
        for(int i = 0; i < 100; i++) {
            Object o = new Object();
            assertNotSame(o.hashCode(), CollectionsMathUtils.hash(o));
        }
    }
}