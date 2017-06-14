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

import com.alesharik.webserver.api.memory.OffHeapVectorBase;

import java.util.Iterator;
import java.util.function.Consumer;

public final class FloatOffHeapVector extends OffHeapVectorBase {
    private static final long FLOAT_SIZE = 4L; //sizeof(float)
    private static final long FLOAT_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(float[].class);
    private static final FloatOffHeapVector INSTANCE = new FloatOffHeapVector();

    public static FloatOffHeapVector instance() {
        return INSTANCE;
    }

    public long fromFloatArray(float[] arr) {
        int length = arr.length;
        long address = unsafe.allocateMemory(length * FLOAT_SIZE + META_SIZE);
        unsafe.copyMemory(arr, FLOAT_ARRAY_BASE_OFFSET, null, address + META_SIZE, length * FLOAT_SIZE);
        unsafe.putLong(address, FLOAT_SIZE);
        unsafe.putLong(address + BASE_FIELD_SIZE + COUNT_FIELD_SIZE, length);
        unsafe.putLong(address + BASE_FIELD_SIZE, length);
        return address;
    }

    /**
     * If size > Integer.MAX_INTEGER, it copy only Integer.MAX_INTEGER elements
     */
    public float[] toFloatArray(long address) {
        long s = size(address);
        float[] arr = new float[(int) s];
        unsafe.copyMemory(null, address + META_SIZE, arr, FLOAT_ARRAY_BASE_OFFSET, arr.length * FLOAT_SIZE);
        return arr;
    }

    /**
     * Return element with given index
     *
     * @param i       element index
     * @param address array pointer (array memory block address)
     */
    public float get(long address, long i) {
        checkIndexBounds(address, i);

        return unsafe.getFloat(address + META_SIZE + i * FLOAT_SIZE);
    }

    /**
     * Add element to end of array and increment it's capacity. If capacity > max, array grow it's capacity and max size
     *
     * @param address array pointer (array memory block address)
     * @param t       object to add
     * @return new address
     */
    public long add(long address, float t) {
        long next = size(address);
        long addr = checkBounds(address, next);

        unsafe.putFloat(addr + META_SIZE + next * FLOAT_SIZE, t);
        incrementSize(addr);

        return addr;
    }

    public boolean contains(long address, float t) {
        return indexOf(address, t) >= 0;
    }

    public Iterator<Float> iterator(long address) {
        return new Iter(address);
    }

    public void forEach(long address, Consumer<Float> consumer) {
        iterator(address).forEachRemaining(consumer);
    }

    public long indexOf(long address, float t) {
        long size = size(address);
        long lastAddress = address + META_SIZE;
        for(long i = 0; i < size; i++) {
            float element = unsafe.getFloat(lastAddress);
            if(Float.compare(element, t) == 0) {
                return i;
            }
            lastAddress += FLOAT_SIZE;
        }
        return -1;
    }

    public long lastIndexOf(long address, float t) {
        long size = size(address);
        long addr = address + META_SIZE;
        for(long i = size - 1; i >= 0; i--) {
            float element = unsafe.getFloat(addr + i * FLOAT_SIZE);
            if(Float.compare(element, t) == 0) {
                return i;
            }
        }
        return -1;
    }

    public float set(long address, long i, float t) {
        checkIndexBounds(address, i);

        float last = unsafe.getFloat(address + META_SIZE + i * FLOAT_SIZE);
        unsafe.putFloat(address + META_SIZE + i * FLOAT_SIZE, t);
        return last;
    }

    public boolean remove(long address, float obj) {
        long index = indexOf(address, obj);
        if(index >= 0) {
            unsafe.copyMemory(address + META_SIZE + FLOAT_SIZE * (index + 1), address + META_SIZE + FLOAT_SIZE * index, FLOAT_SIZE * (size(address) - index));
            decrementSize(address);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected long getElementSize() {
        return FLOAT_SIZE; //sizeof(float)
    }

    private class Iter implements Iterator<Float> {
        private final long address;
        private long pos;

        public Iter(long address) {
            this.address = address;
            this.pos = 0;
        }

        @Override
        public boolean hasNext() {
            return pos < size(address);
        }

        @Override
        public Float next() {
            if(!hasNext()) {
                return null;
            }
            float res = get(address, pos);
            pos++;
            return res;
        }
    }
}
