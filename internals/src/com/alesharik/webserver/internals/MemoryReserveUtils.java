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

package com.alesharik.webserver.internals;

import lombok.experimental.UtilityClass;
import sun.misc.Cleaner;
import sun.misc.VM;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This is a wrapper for {@link java.nio.Bits#reserveMemory(long, int)} and {@link java.nio.Bits#unreserveMemory(long, int)}
 */
@UtilityClass
public class MemoryReserveUtils {
    private static final Class<?> bitsClazz;
    private static final MethodHandle reserveMemoryMethod;
    private static final MethodHandle unreserveMemoryMethod;

    static {
        try {
            bitsClazz = Class.forName("java.nio.Bits");
            Method reserveMemory = bitsClazz.getDeclaredMethod("reserveMemory", long.class, int.class);
            Method unreserveMemory = bitsClazz.getDeclaredMethod("unreserveMemory", long.class, int.class);

            reserveMemory.setAccessible(true);
            unreserveMemory.setAccessible(true);

            reserveMemoryMethod = MethodHandles.lookup().unreflect(reserveMemory);
            unreserveMemoryMethod = MethodHandles.lookup().unreflect(unreserveMemory);
        } catch (ClassNotFoundException e) {
            throw new InternalHackingError("java.nio.Bits class not found!", e);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new InternalHackingError("Illegal access to java.nio.Bits", e);
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
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
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
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
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
    private static void reserveMemory1(int size) throws Throwable {
        boolean pa = VM.isDirectMemoryPageAligned();
        int ps = UnsafeAccess.INSTANCE.pageSize();
        long s = Math.max(1L, (long) size + (pa ? ps : 0));
        reserveMemoryMethod.invokeExact(s, size);
    }

    /**
     * Call {@link java.nio.Bits#unreserveMemory(long, int)}
     *
     * @param size memory size to unreserve
     * @throws InvocationTargetException if anything like OOME happens
     */
    private static void unreserveMemory(int size) throws Throwable {
        boolean pa = VM.isDirectMemoryPageAligned();
        int ps = UnsafeAccess.INSTANCE.pageSize();
        long s = Math.max(1L, (long) size + (pa ? ps : 0));
        unreserveMemoryMethod.invokeExact(s, size);
    }
}
