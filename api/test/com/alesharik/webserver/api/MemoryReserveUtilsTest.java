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

package com.alesharik.webserver.api;

import org.junit.Test;
import sun.misc.Cleaner;
import sun.misc.SharedSecrets;

import static org.junit.Assert.assertEquals;

public class MemoryReserveUtilsTest {

    @Test
    public void reserveMemoryTest() throws Exception {
        long first = SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();

        Cleaner cleaner = MemoryReserveUtils.reserveMemory(this, 120);
        long second = SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();
        assertEquals(second, first + 120);

        cleaner.clean();
        long third = SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();
        assertEquals(first, third);
    }

    @Test
    public void reserveBigMemoryTest() throws Exception {
        long freeMemory = Runtime.getRuntime().freeMemory();
        long bigReserveSize = (long) Integer.MAX_VALUE + 512;
        if(freeMemory < bigReserveSize) {
            System.out.println("Can't start reserveBigMemoryTest! Don't have enough memory!");
        } else {
            long first = SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();

            Cleaner cleaner = MemoryReserveUtils.reserveMemory(this, bigReserveSize);
            long second = SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();
            assertEquals(second, first + bigReserveSize);

            cleaner.clean();
            long third = SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();
            assertEquals(first, third);
        }
    }

    @Test(expected = OutOfMemoryError.class)
    public void reserveOutOfMemoryErrorTest() throws Exception {
        MemoryReserveUtils.reserveMemory(this, Long.MAX_VALUE);
    }

    @Test
    public void unreserveMemoryTest() throws Exception {
        long first = SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();

        MemoryReserveUtils.reserveMemory(this, 120);
        long second = SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();
        assertEquals(second, first + 120);

        MemoryReserveUtils.unreserveMemory(120);
        long third = SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();
        assertEquals(first, third);
    }

    @Test
    public void unreserveBigMemoryTest() throws Exception {
        long freeMemory = Runtime.getRuntime().freeMemory();
        long bigReserveSize = (long) Integer.MAX_VALUE + 512;
        if(freeMemory < bigReserveSize) {
            System.out.println("Can't start unreserveBigMemoryTest! Don't have enough memory!");
        } else {
            long first = SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();

            MemoryReserveUtils.reserveMemory(this, bigReserveSize);
            long second = SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();
            assertEquals(second, first + bigReserveSize);

            MemoryReserveUtils.unreserveMemory(bigReserveSize);
            long third = SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();
            assertEquals(first, third);
        }
    }
}