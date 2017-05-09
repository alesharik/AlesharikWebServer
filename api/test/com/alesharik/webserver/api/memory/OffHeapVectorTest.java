package com.alesharik.webserver.api.memory;

import com.alesharik.webserver.api.memory.impl.ByteOffHeapVector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class OffHeapVectorTest {
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
    public void tearDown() throws Exception {
        array.free(address);
    }

    @Test
    public void allocateAndFree() throws Exception {
        long address = array.allocate();
        assertNotSame(address, 0);
        array.free(address);
    }

    @Test
    public void getSize() throws Exception {
        assertEquals(array.size(address), 4);
    }

    @Test
    public void addAndGet() throws Exception {
        array.add(address, (byte) 0x14);
        assertEquals((byte) array.get(address, 4), (byte) 0x14);
    }

    @Test
    public void addAndGetWithResize() throws Exception {
        long arrAddress = array.allocate();

        for(int i = 0; i < 128; i++) {
            arrAddress = array.add(arrAddress, (byte) 0x10);
        }

        for(int i = 0; i < 128; i++) {
            assertEquals((byte) array.get(arrAddress, i), (byte) 0x10);
        }

        array.free(arrAddress);
    }

    @Test
    public void contains() throws Exception {
        assertTrue(array.contains(address, (byte) 0x11));
        assertFalse(array.contains(address, (byte) 0x03));
    }

    @Test
    public void iteratorNext() throws Exception {
        Iterator<Byte> iterator = array.iterator(address);
        int size = 0;
        while(iterator.next() != null) {
            size++;
        }
        assertEquals(size, array.size(address));
    }

    @Test
    public void iteratorHasNext() throws Exception {
        Iterator<Byte> iterator = array.iterator(address);
        int size = 0;
        while(iterator.hasNext()) {
            iterator.next();
            size++;
        }
        assertEquals(size, array.size(address));
    }

    @Test
    public void forEach() throws Exception {
        AtomicInteger size = new AtomicInteger();
        array.forEach(address, aByte -> size.incrementAndGet());
        assertEquals(size.get(), array.size(address));
    }

    @Test
    public void indexOf() throws Exception {
        assertEquals(array.indexOf(address, (byte) 0x11), 0);
    }

    @Test
    public void lastIndexOf() throws Exception {
        assertEquals(array.lastIndexOf(address, (byte) 0x11), 3);
    }

    @Test
    public void set() throws Exception {
        array.set(address, 1, (byte) 0x01);
        assertEquals((byte) array.get(address, 1), (byte) 0x01);
    }

    @Test
    public void remove() throws Exception {
        array.remove(address, (byte) 0x10);
        assertFalse(array.contains(address, (byte) 0x10));
    }

    @Test
    public void isEmpty() throws Exception {
        assertFalse(array.isEmpty(address));
    }

    @Test
    public void shrinkTest() throws Exception {
        address = array.shrink(address);
        assertEquals(array.size(address), array.getMax(address));
    }
}