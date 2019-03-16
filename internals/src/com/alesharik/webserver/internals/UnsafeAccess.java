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


import java.lang.reflect.Field;

public abstract class UnsafeAccess {
    public static final UnsafeAccess INSTANCE;

    static {
        if(unsafeSupported())
            try {
                INSTANCE = (UnsafeAccess) Class.forName("com.alesharik.webserver.internals.DefaultUnsafeAccess").newInstance();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                throw new UnsafeAccessError(e);
            }
        else
            INSTANCE = null;//FIXME
    }

    private static boolean unsafeSupported() {
        try {
            Class<?> clazz = Class.forName("sun.misc.Unsafe");
            Field field = clazz.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Object o = field.get(null);
            return o != null;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }

    /**
     * Create object without calling constructor
     */
    public abstract Object newInstance(Class<?> clazz);

    public abstract int pageSize();

    public abstract long allocateMemory(long bytes);

    public abstract void putLong(long address, long l);

    public abstract long getLong(long address);

    public abstract void putShort(long address, short l);

    public abstract short getShort(long address);

    public abstract void putInt(long address, int l);

    public abstract int getInt(long address);

    public abstract void putByte(long address, byte l);

    public abstract byte getByte(long address);

    public abstract void putBoolean(long address, boolean l);

    public abstract boolean getBoolean(long address);

    public abstract void putChar(long address, char l);

    public abstract char getChar(long address);

    public abstract void putFloat(long address, float l);

    public abstract float getFloat(long address);

    public abstract void putDouble(long address, double l);

    public abstract double getDouble(long address);

    public abstract void putLong(Object o, long offset, long l);

    public abstract long getLong(Object o, long offset);

    public abstract void putShort(Object o, long offset, short l);

    public abstract short getShort(Object o, long offset);

    public abstract void putInt(Object o, long offset, int l);

    public abstract int getInt(Object o, long offset);

    public abstract void putByte(Object o, long offset, byte l);

    public abstract byte getByte(Object o, long offset);

    public abstract void putBoolean(Object o, long offset, boolean l);

    public abstract boolean getBoolean(Object o, long offset);

    public abstract void putChar(Object o, long offset, char l);

    public abstract char getChar(Object o, long offset);

    public abstract void putFloat(Object o, long offset, float l);

    public abstract float getFloat(Object o, long offset);

    public abstract void putDouble(Object o, long offset, double l);

    public abstract double getDouble(Object o, long offset);

    public abstract void putObject(Object o, long offset, Object l);

    public abstract Object getObject(Object o, long offset);

    public abstract long staticFieldOffset(Field field);

    public abstract long objectFieldOffset(Field field);

    public abstract long reallocateMemory(long addr, long size);

    public abstract void copyMemory(Object src, long srcOff, Object dst, long dstOff, long len);

    public abstract int arrayBaseOffset(Class<?> c);

    public abstract void freeMemory(long address);

    public abstract byte[] toByteArray(long address, int size);
}
