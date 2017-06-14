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

import com.alesharik.webserver.api.collections.TripleHashMap;
import lombok.SneakyThrows;
import one.nio.util.JavaInternals;
import sun.misc.Unsafe;

import javax.annotation.concurrent.NotThreadSafe;

public class Struct {
    private static final Unsafe unsafe = JavaInternals.getUnsafe();

    private final TripleHashMap<String, Type, Long> types;
    private final long size;

    private Struct(TripleHashMap<String, Type, Long> types, long size) {
        this.types = types;
        this.size = size;
    }

    public long allocate() {
        return unsafe.allocateMemory(size);
    }

    public void free(long address) {
        unsafe.freeMemory(address);
    }

    public long getSize() {
        return size;
    }

    public void setLong(long address, String name, long l) {
        unsafe.putLong(getObjectAddress(address, name, Type.LONG), l);
    }

    public long getLong(long address, String name) {
        return unsafe.getLong(getObjectAddress(address, name, Type.LONG));
    }

    public void setBoolean(long address, String name, boolean b) {
        unsafe.putInt(getObjectAddress(address, name, Type.BOOLEAN), b ? 1 : 0);
    }

    public boolean getBoolean(long address, String name) {
        return unsafe.getInt(getObjectAddress(address, name, Type.BOOLEAN)) == 1;
    }

    public void setByte(long address, String name, byte b) {
        unsafe.putByte(getObjectAddress(address, name, Type.BYTE), b);
    }

    public byte getByte(long address, String name) {
        return unsafe.getByte(getObjectAddress(address, name, Type.BYTE));
    }

    public void setShort(long address, String name, short s) {
        unsafe.putShort(getObjectAddress(address, name, Type.SHORT), s);
    }

    public short getShort(long address, String name) {
        return unsafe.getShort(getObjectAddress(address, name, Type.SHORT));
    }

    public void setChar(long address, String name, char c) {
        unsafe.putChar(getObjectAddress(address, name, Type.CHAR), c);
    }

    public char getChar(long address, String name) {
        return unsafe.getChar(getObjectAddress(address, name, Type.CHAR));
    }

    public void setDouble(long address, String name, double d) {
        unsafe.putDouble(getObjectAddress(address, name, Type.DOUBLE), d);
    }

    public double getDouble(long address, String name) {
        return unsafe.getDouble(getObjectAddress(address, name, Type.DOUBLE));
    }

    public void setFloat(long address, String name, float f) {
        unsafe.putFloat(getObjectAddress(address, name, Type.FLOAT), f);
    }

    public float getFloat(long address, String name) {
        return unsafe.getFloat(getObjectAddress(address, name, Type.FLOAT));
    }

    public void setStruct(long address, String name, Struct struct, long structAddress) {
        if(types.getAddition(name) != struct.getSize()) {
            throw new IllegalArgumentException("Struct have another size!");
        }
        unsafe.copyMemory(structAddress, getObjectAddress(address, name, Type.STRUCT), struct.getSize());
    }

    public long getStructAddress(long address, String name) {
        return getObjectAddress(address, name, Type.STRUCT);
    }

    public void setArray(long address, String name, long arrayAddress) {
        setPointer(address, name, arrayAddress);
    }

    public long getArrayAddress(long address, String name) {
        return getObjectAddress(address, name, Type.POINTER);
    }

    public void setPointer(long address, String name, long pointer) {
        unsafe.putAddress(getObjectAddress(address, name, Type.POINTER), pointer);
    }

    public long getPointer(long address, String name) {
        return unsafe.getAddress(getObjectAddress(address, name, Type.POINTER));
    }

    private long getObjectAddress(long address, String name, Type type) {
        if(types.get(name) != type) {
            throw new IllegalArgumentException("Illegal type!");
        }
        Long offset = types.getAddition(name);
        if(offset == null) {
            throw new IllegalArgumentException(name + " parameter not found!");
        } else {
            return address + offset;
        }
    }

    @NotThreadSafe
    public static class Builder {
        private final TripleHashMap<String, Type, Long> types;
        private long offset;

        public Builder() {
            types = new TripleHashMap<>();
            offset = 0;
        }

        public Builder addInteger(String name) {
            types.put(name, Type.INTEGER, offset);
            offset += 4;
            return this;
        }

        public Builder addLong(String name) {
            types.put(name, Type.LONG, offset);
            offset += 8;
            return this;
        }

        public Builder addBoolean(String name) {
            types.put(name, Type.BOOLEAN, offset);
            offset += 4; //Same as integer
            return this;
        }

        public Builder addByte(String name) {
            types.put(name, Type.BYTE, offset);
            offset += 1;
            return this;
        }

        public Builder addShort(String name) {
            types.put(name, Type.SHORT, offset);
            offset += 2;
            return this;
        }

        public Builder addChar(String name) {
            types.put(name, Type.CHAR, offset);
            offset += 2;
            return this;
        }

        public Builder addDouble(String name) {
            types.put(name, Type.DOUBLE, offset);
            offset += 8;
            return this;
        }

        public Builder addFloat(String name) {
            types.put(name, Type.FLOAT, offset);
            offset += 4;
            return this;
        }

        public Builder addStruct(String name, Struct struct) {
            types.put(name, Type.STRUCT, offset);
            offset += struct.getSize();
            return this;
        }

        public Builder addArray(String name) {
            types.put(name, Type.POINTER, offset); //Because we can't hold dynamic array
            offset += 8; //Array pointer
            return this;
        }

        public Builder addPointer(String name) {
            types.put(name, Type.POINTER, offset);
            offset += 8;
            return this;
        }

        @SuppressWarnings("unchecked")
        @SneakyThrows
        public Struct build() {
            return new Struct(types.clone(), offset);
        }
    }

    private enum Type {
        LONG,
        INTEGER,
        SHORT,
        DOUBLE,
        FLOAT,
        BYTE,
        CHAR,
        BOOLEAN,
        STRUCT,
        POINTER
    }
}
