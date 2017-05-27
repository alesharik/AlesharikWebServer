package com.alesharik.webserver;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import one.nio.util.JavaInternals;
import sun.misc.Cleaner;
import sun.misc.VM;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This is a wrapper for {@link java.nio.Bits#reserveMemory(long, int)} and {@link java.nio.Bits#unreserveMemory(long, int)}
 */
@UtilityClass
public class MemoryReserveUtils {
    private static final Class<?> bitsClazz;
    private static final Method reserveMemoryMethod;
    private static final Method unreserveMemoryMethod;

    static {
        try {
            bitsClazz = Class.forName("java.nio.Bits");
            reserveMemoryMethod = bitsClazz.getDeclaredMethod("reserveMemory", long.class, int.class);
            unreserveMemoryMethod = bitsClazz.getDeclaredMethod("unreserveMemory", long.class, int.class);

            reserveMemoryMethod.setAccessible(true);
            unreserveMemoryMethod.setAccessible(true);
        } catch (ClassNotFoundException e) {
            throw new Error("java.nio.Bits class not found!");
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }

    /**
     * Reserve direct memory for object
     *
     * @param obj  object for memory reservation
     * @param size memory size to reserve
     * @return cleaner, which cleans memory before <code>obj</code> deletion
     */
    public static Cleaner reserveMemory(Object obj, long size) {
        reserveMemory(size);
        return Cleaner.create(obj, () -> unreserveMemory(size));
    }

    /**
     * Reserve direct memory. You need to clean it after using it!
     *
     * @param size memory size to reserve
     */
    public static void reserveMemory(long size) {
        while(size > 0) {
            int delta = (int) Math.min(Integer.MAX_VALUE, size);
            size -= delta;
            try {
                reserveMemory1(delta);
            } catch (InvocationTargetException e) {
                if(e.getCause() instanceof Error)
                    throw (Error) e.getCause();
                else
                    throw new RuntimeException(e.getCause());
            }
        }
    }

    public static void unreserveMemory(long size) {
        while(size > 0) {
            int delta = (int) Math.min(Integer.MAX_VALUE, size);
            size -= delta;
            try {
                unreserveMemory(delta);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        }
    }

    /**
     * Call {@link java.nio.Bits#reserveMemory(long, int)}
     *
     * @param size memory size to reserve
     * @throws InvocationTargetException if anything like OOME happens
     */
    @SneakyThrows
    private static void reserveMemory1(int size) throws InvocationTargetException {
        boolean pa = VM.isDirectMemoryPageAligned();
        int ps = JavaInternals.unsafe.pageSize();
        long s = Math.max(1L, (long) size + (pa ? ps : 0));
        reserveMemoryMethod.invoke(null, s, size);
    }

    /**
     * Call {@link java.nio.Bits#unreserveMemory(long, int)}
     *
     * @param size memory size to unreserve
     * @throws InvocationTargetException if anything like OOME happens
     */
    @SneakyThrows
    private static void unreserveMemory(int size) throws InvocationTargetException {
        boolean pa = VM.isDirectMemoryPageAligned();
        int ps = JavaInternals.unsafe.pageSize();
        long s = Math.max(1L, (long) size + (pa ? ps : 0));
        unreserveMemoryMethod.invoke(null, s, size);
    }
}
