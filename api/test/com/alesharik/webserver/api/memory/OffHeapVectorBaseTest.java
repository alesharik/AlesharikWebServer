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

package com.alesharik.webserver.api.memory;

import com.alesharik.webserver.api.memory.impl.ByteOffHeapVector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sun.misc.SharedSecrets;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class OffHeapVectorBaseTest {
    private static final ByteOffHeapVector array = new ByteOffHeapVector();
    private long address;

    @Before
    public void setUp() throws Exception {
        address = array.allocate();
        address = array.add(address, (byte) 0x11);
        address = array.add(address, (byte) 0x10);
        address = array.add(address, (byte) 0x09);
        address = array.add(address, (byte) 0x11);
    }

    @After
    public void tearDown() {
        array.free(address);
    }

    @Test
    public void allocateAndFree() {
        long address = array.allocate();
        assertNotSame(address, 0);
        array.free(address);
    }

    @Test
    public void getSize() {
        assertEquals(array.size(address), 4);
    }

    @Test
    public void addAndGet() {
        array.add(address, (byte) 0x14);
        assertEquals(array.get(address, 4), (byte) 0x14);
    }

    @Test
    public void addAndGetWithResize() {
        long arrAddress = array.allocate();

        for(int i = 0; i < 128; i++) {
            arrAddress = array.add(arrAddress, (byte) 0x10);
        }

        for(int i = 0; i < 128; i++) {
            assertEquals(array.get(arrAddress, i), (byte) 0x10);
        }

        array.free(arrAddress);
    }

    @Test
    public void contains() {
        assertTrue(array.contains(address, (byte) 0x11));
        assertFalse(array.contains(address, (byte) 0x03));
    }

    @Test
    public void iteratorNext() {
        Iterator<Byte> iterator = array.iterator(address);
        int size = 0;
        while(iterator.next() != null) {
            size++;
        }
        assertEquals(size, array.size(address));
    }

    @Test
    public void iteratorHasNext() {
        Iterator<Byte> iterator = array.iterator(address);
        int size = 0;
        while(iterator.hasNext()) {
            iterator.next();
            size++;
        }
        assertEquals(size, array.size(address));
    }

    @Test
    public void forEach() {
        AtomicInteger size = new AtomicInteger();
        array.forEach(address, aByte -> size.incrementAndGet());
        assertEquals(size.get(), array.size(address));
    }

    @Test
    public void indexOf() {
        assertEquals(array.indexOf(address, (byte) 0x11), 0);
    }

    @Test
    public void lastIndexOf() {
        assertEquals(array.lastIndexOf(address, (byte) 0x11), 3);
    }

    @Test
    public void set() {
        array.set(address, 1, (byte) 0x01);
        assertEquals(array.get(address, 1), (byte) 0x01);
    }

    @Test
    public void remove() {
        array.remove(address, (byte) 0x10);
        assertFalse(array.contains(address, (byte) 0x10));
    }

    @Test
    public void isEmpty() {
        assertFalse(array.isEmpty(address));

        long addr = array.allocate();
        assertTrue(array.isEmpty(addr));
        array.free(addr);
    }

    @Test
    public void shrinkTest() {
        address = array.shrink(address);
        assertEquals(array.size(address), array.getMax(address));

        long arr = array.allocate();
        for(int i = 0; i < 16; i++) {
            array.add(arr, (byte) i);
        }
        array.shrink(arr);
        assertEquals(array.size(arr), array.getMax(arr));
        array.free(arr);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void checkBoundsTestIMinus() {
        array.checkIndexBounds(address, -1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void checkIndexBoundsIMoreThanSize() {
        array.checkIndexBounds(address, Integer.MAX_VALUE);
    }


    @Test
    public void removeExists() {
        assertTrue(array.remove(address, 0));
    }

    @Test
    public void removeNotExists() {
        assertFalse(array.remove(address, Integer.MAX_VALUE));
        assertFalse(array.remove(address, -1));
    }

    @Test
    public void reserveUnreserveMemoryTest() {
        long first = SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();

        long addr = array.allocate();
        long second = SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();
        assertEquals(first + OffHeapVectorBase.META_SIZE + OffHeapVectorBase.DEFAULT_INITIAL_COUNT, second);

        addr = array.resize(addr, 32);
        long third = SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();
        assertEquals(first + OffHeapVectorBase.META_SIZE + 32, third);

        array.free(addr);
        long fourth = SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();
        assertEquals(first, fourth);
    }
}