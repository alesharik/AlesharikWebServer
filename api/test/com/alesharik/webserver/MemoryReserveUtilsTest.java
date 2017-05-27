package com.alesharik.webserver;

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