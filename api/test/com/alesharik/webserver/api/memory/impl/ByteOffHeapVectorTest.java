package com.alesharik.webserver.api.memory.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
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
    public void tearDown() throws Exception {
        array.free(address);
    }

    @Test
    public void formByteArray() throws Exception {
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
    public void toByteArray() throws Exception {
        byte[] arr = array.toByteArray(address);
        assertEquals(arr[0], FIRST_VALUE);
        assertEquals(arr[1], SECOND_VALUE);
        assertEquals(arr[2], THIRD_VALUE);
        assertEquals(arr[3], FOURTH_VALUE);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void getILessThanZero() throws Exception {
        array.get(address, -1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void getIMoreThanCount() throws Exception {
        array.get(address, 300);
    }

    @Test
    public void getNormal() throws Exception {
        assertSame(array.get(address, 0), FIRST_VALUE);
        assertSame(array.get(address, 1), SECOND_VALUE);
        assertSame(array.get(address, 2), THIRD_VALUE);
        assertSame(array.get(address, 3), FOURTH_VALUE);
    }

    @Test
    public void addTest() throws Exception {
        for(int i = 0; i < 200; i++) {
            address = array.add(address, DUDE);
        }
    }

    @Test
    public void containsTest() throws Exception {
        assertTrue(array.contains(address, FIRST_VALUE));
        assertTrue(array.contains(address, SECOND_VALUE));
        assertTrue(array.contains(address, THIRD_VALUE));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void iteratorTest() throws Exception {
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

        iter.remove();
    }

    @Test
    public void forEachTest() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        array.forEach(address, aByte -> counter.getAndIncrement());
        assertTrue(counter.get() > 3);
    }

    @Test
    public void indexOfExists() throws Exception {
        assertEquals(array.indexOf(address, FIRST_VALUE), 0);
        assertEquals(array.indexOf(address, SECOND_VALUE), 1);
        assertEquals(array.indexOf(address, THIRD_VALUE), 2);
        assertEquals(array.indexOf(address, FOURTH_VALUE), 0);
    }

    @Test
    public void indexOfNotExists() throws Exception {
        assertEquals(array.indexOf(address, NOT_EXISTS), -1);
    }

    @Test
    public void lastIndexOfExists() throws Exception {
        assertEquals(array.lastIndexOf(address, FOURTH_VALUE), 3);
        assertEquals(array.lastIndexOf(address, SECOND_VALUE), 1);
        assertEquals(array.lastIndexOf(address, THIRD_VALUE), 2);
    }

    @Test
    public void lastIndexOfNotExists() throws Exception {
        assertEquals(array.lastIndexOf(address, NOT_EXISTS), -1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void setLessThanZero() throws Exception {
        array.set(address, -1, DUDE);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void setMoreThanCount() throws Exception {
        array.set(address, 1000, DUDE);
    }

    @Test
    public void setNormal() throws Exception {
        array.set(address, 4, SECOND_VALUE);
        assertEquals(SECOND_VALUE, array.get(address, 4));
    }

    public void removeExists() throws Exception {
        assertTrue(array.remove(address, DUDE));
    }

    @Test
    public void removeNotExists() throws Exception {
        assertFalse(array.remove(address, NOT_EXISTS));
    }

    @Test
    public void getElementSizeTest() throws Exception {
        assertEquals(array.getElementSize(), 1L);
    }
}