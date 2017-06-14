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

public final class DoubleOffHeapVector extends OffHeapVectorBase {
    private static final long DOUBLE_SIZE = 8L; //sizeof(double)
    private static final long DOUBLE_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(double[].class);
    private static final DoubleOffHeapVector INSTANCE = new DoubleOffHeapVector();

    public static DoubleOffHeapVector instance() {
        return INSTANCE;
    }

    public long fromDoubleArray(double[] arr) {
        int length = arr.length;
        long address = unsafe.allocateMemory(length * DOUBLE_SIZE + META_SIZE);
        unsafe.copyMemory(arr, DOUBLE_ARRAY_BASE_OFFSET, null, address + META_SIZE, length * DOUBLE_SIZE);
        unsafe.putLong(address, DOUBLE_SIZE);
        unsafe.putLong(address + BASE_FIELD_SIZE + COUNT_FIELD_SIZE, length);
        unsafe.putLong(address + BASE_FIELD_SIZE, length);
        return address;
    }

    /**
     * If size > Integer.MAX_INTEGER, it copy only Integer.MAX_INTEGER elements
     */
    public double[] toDoubleArray(long address) {
        long s = size(address);
        double[] arr = new double[(int) s];
        unsafe.copyMemory(null, address + META_SIZE, arr, DOUBLE_ARRAY_BASE_OFFSET, arr.length * DOUBLE_SIZE);
        return arr;
    }

    /**
     * Return element with given index
     *
     * @param i       element index
     * @param address array pointer (array memory block address)
     */
    public double get(long address, long i) {
        checkIndexBounds(address, i);

        return unsafe.getDouble(address + META_SIZE + i * DOUBLE_SIZE);
    }

    /**
     * Add element to end of array and increment it's capacity. If capacity > max, array grow it's capacity and max size
     *
     * @param address array pointer (array memory block address)
     * @param t       object to add
     * @return new address
     */
    public long add(long address, double t) {
        long next = size(address);
        long addr = checkBounds(address, next);

        unsafe.putDouble(addr + META_SIZE + next * DOUBLE_SIZE, t);
        incrementSize(addr);

        return addr;
    }

    public boolean contains(long address, double t) {
        return indexOf(address, t) >= 0;
    }

    public Iterator<Double> iterator(long address) {
        return new Iter(address);
    }

    public void forEach(long address, Consumer<Double> consumer) {
        iterator(address).forEachRemaining(consumer);
    }

    public long indexOf(long address, double t) {
        long size = size(address);
        long lastAddress = address + META_SIZE;
        for(long i = 0; i < size; i++) {
            double element = unsafe.getDouble(lastAddress);
            if(Double.compare(element, t) == 0) {
                return i;
            }
            lastAddress += DOUBLE_SIZE;
        }
        return -1;
    }

    public long lastIndexOf(long address, double t) {
        long size = size(address);
        long addr = address + META_SIZE;
        for(long i = size - 1; i >= 0; i--) {
            double element = unsafe.getDouble(addr + i * DOUBLE_SIZE);
            if(Double.compare(element, t) == 0) {
                return i;
            }
        }
        return -1;
    }

    public double set(long address, long i, double t) {
        checkIndexBounds(address, i);

        double last = unsafe.getDouble(address + META_SIZE + i * DOUBLE_SIZE);
        unsafe.putDouble(address + META_SIZE + i * DOUBLE_SIZE, t);
        return last;
    }

    public boolean remove(long address, double obj) {
        long index = indexOf(address, obj);
        if(index >= 0) {
            unsafe.copyMemory(address + META_SIZE + DOUBLE_SIZE * (index + 1), address + META_SIZE + DOUBLE_SIZE * index, DOUBLE_SIZE * (size(address) - index));
            decrementSize(address);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected long getElementSize() {
        return DOUBLE_SIZE; //sizeof(double)
    }

    private class Iter implements Iterator<Double> {
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
        public Double next() {
            if(!hasNext()) {
                return null;
            }
            double res = get(address, pos);
            pos++;
            return res;
        }
    }
}
