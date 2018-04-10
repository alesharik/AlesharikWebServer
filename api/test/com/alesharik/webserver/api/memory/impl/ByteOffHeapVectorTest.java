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

package com.alesharik.webserver.api.memory.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ByteOffHeapVectorTest {
    private static final byte FIRST_VALUE = (byte) 0x11;
    private static final byte SECOND_VALUE = (byte) 0x10;
    private static final byte THIRD_VALUE = (byte) 0x09;
    private static final byte FOURTH_VALUE = (byte) 0x11;
    private static final byte DUDE = (byte) 0x08;
    private static final byte NOT_EXISTS = (byte) 0x02;

    private static final ByteOffHeapVector array = new ByteOffHeapVector();
    private long address;

    @Before
    public void setUp() throws Exception {
        address = array.allocate();
        address = array.add(address, FIRST_VALUE);
        address = array.add(address, SECOND_VALUE);
        address = array.add(address, THIRD_VALUE);
        address = array.add(address, FOURTH_VALUE);
        address = array.add(address, DUDE); //i = 4
    }

    @After
    public void tearDown() {
        array.free(address);
    }

    @Test
    public void instanceTest() {
        ByteOffHeapVector v = ByteOffHeapVector.instance();
        long address = v.allocate();
        v.add(address, FIRST_VALUE);
        assertEquals(v.get(address, 0), FIRST_VALUE);
        v.free(address);
    }

    @Test
    public void formByteArray() {
        byte[] arr = {FIRST_VALUE, SECOND_VALUE, THIRD_VALUE, FOURTH_VALUE};
        long addr = 0;
        try {
            addr = array.fromByteArray(arr);
            assertEquals(array.get(addr, 0), FIRST_VALUE);
            assertEquals(array.get(addr, 1), SECOND_VALUE);
            assertEquals(array.get(addr, 2), THIRD_VALUE);
            assertEquals(array.get(addr, 3), FOURTH_VALUE);
        } finally {
            array.free(addr);
        }
    }

    @Test
    public void toByteArray() {
        byte[] arr = array.toByteArray(address);
        assertEquals(arr[0], FIRST_VALUE);
        assertEquals(arr[1], SECOND_VALUE);
        assertEquals(arr[2], THIRD_VALUE);
        assertEquals(arr[3], FOURTH_VALUE);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void getILessThanZero() {
        array.get(address, -1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void getIMoreThanCount() {
        array.get(address, 300);
    }

    @Test
    public void getNormal() {
        assertSame(array.get(address, 0), FIRST_VALUE);
        assertSame(array.get(address, 1), SECOND_VALUE);
        assertSame(array.get(address, 2), THIRD_VALUE);
        assertSame(array.get(address, 3), FOURTH_VALUE);
    }

    @Test
    public void addTest() {
        for(int i = 0; i < 200; i++) {
            address = array.add(address, DUDE);
        }
    }

    @Test
    public void containsTest() {
        assertTrue(array.contains(address, FIRST_VALUE));
        assertTrue(array.contains(address, SECOND_VALUE));
        assertTrue(array.contains(address, THIRD_VALUE));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void iteratorTest() {
        Iterator<Byte> iter = array.iterator(address);
        assertTrue(iter.hasNext());
        assertSame(iter.next(), FIRST_VALUE);

        assertTrue(iter.hasNext());
        assertSame(iter.next(), SECOND_VALUE);

        assertTrue(iter.hasNext());
        assertSame(iter.next(), THIRD_VALUE);

        assertTrue(iter.hasNext());

        AtomicInteger counter = new AtomicInteger(0);
        iter.forEachRemaining(a -> counter.incrementAndGet());
        assertTrue(counter.get() > 0);

        assertNull(iter.next());

        iter.remove();
    }

    @Test
    public void forEachTest() {
        AtomicInteger counter = new AtomicInteger(0);
        array.forEach(address, aByte -> counter.getAndIncrement());
        assertTrue(counter.get() > 3);
    }

    @Test
    public void indexOfExists() {
        assertEquals(array.indexOf(address, FIRST_VALUE), 0);
        assertEquals(array.indexOf(address, SECOND_VALUE), 1);
        assertEquals(array.indexOf(address, THIRD_VALUE), 2);
        assertEquals(array.indexOf(address, FOURTH_VALUE), 0);
    }

    @Test
    public void indexOfNotExists() {
        assertEquals(array.indexOf(address, NOT_EXISTS), -1);
    }

    @Test
    public void lastIndexOfExists() {
        assertEquals(array.lastIndexOf(address, FOURTH_VALUE), 3);
        assertEquals(array.lastIndexOf(address, SECOND_VALUE), 1);
        assertEquals(array.lastIndexOf(address, THIRD_VALUE), 2);
    }

    @Test
    public void lastIndexOfNotExists() {
        assertEquals(array.lastIndexOf(address, NOT_EXISTS), -1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void setLessThanZero() {
        array.set(address, -1, DUDE);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void setMoreThanCount() {
        array.set(address, 1000, DUDE);
    }

    @Test
    public void setNormal() {
        array.set(address, 4, SECOND_VALUE);
        assertEquals(SECOND_VALUE, array.get(address, 4));
    }

    @Test
    public void removeExists() {
        assertTrue(array.remove(address, DUDE));
    }

    @Test
    public void removeNotExists() {
        assertFalse(array.remove(address, NOT_EXISTS));
    }

    @Test
    public void getElementSizeTest() {
        assertEquals(array.getElementSize(), 1L);
    }

    @Test
    public void writeTest() {
        byte[] test = new byte[24];
        new Random(1).nextBytes(test);
        address = array.write(address, test, 1, 18);
        for(int i = 5, j = 0; i < 17 + 5; i++, j++) {
            assertEquals(test[j + 1], array.get(address, i));
        }
    }

    @Test
    public void basicWriteTest() {
        byte[] test = new byte[24];
        new Random(1).nextBytes(test);
        address = array.write(address, test);
        for(int i = 5, j = 0; i < 24 + 5; i++, j++) {
            assertEquals(test[j], array.get(address, i));
        }
    }

    @Test
    public void testBigHeap() {
        for(int i = 0; i < 1_048_576; i++) {
            address = array.add(address, (byte) i);
        }
    }

    @Test
    public void testErrorResizeMoreThanMax() {
        for(int i = 0; i < 1_048_576; i++) {
            address = array.add(address, (byte) i);
        }
        byte[] arr = array.toByteArray(address);
        address = array.write(address, arr);

        array.clear(address);
    }

    @Test
    public void testCut() {
        array.clear(address);
        for(int i = 0; i < 100; i++) {
            address = array.add(address, (byte) i);
        }
        byte[] cut = array.cut(address, 25);
        assertEquals(25, cut.length);
        for(int i = 0; i < 25; i++) {
            assertEquals((byte) i, cut[i]);
        }
        assertEquals(75, array.size(address));
        array.cut(address, 10);
        assertEquals(65, array.size(address));
        for(int i = 35; i < 65; i++) {
            assertEquals(((byte) i), array.get(address, i - 35));
        }
    }

    @Test
    public void testCutAll() {
        array.clear(address);
        for(int i = 0; i < 100; i++) {
            address = array.add(address, (byte) i);
        }
        byte[] data = array.cut(address, 100);
        assertEquals(100, data.length);
        assertEquals(0, array.size(address));

        for(int i = 0; i < 100; i++) {
            assertEquals((byte) i, data[i]);
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCutNegativeSize() {
        array.clear(address);
        array.cut(address, 10000);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testWriteWithNegativeSize() {
        array.clear(address);
        array.write(address, new byte[1000], 0, -1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testWriteWithNegativeOffset() {
        array.clear(address);
        array.write(address, new byte[1000], -1, 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testWriteWithIllegalSize() {
        array.clear(address);
        array.write(address, new byte[1000], 0, 2000);
    }
}