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

public final class IntegerOffHeapVector extends OffHeapVectorBase {
    private static final long INT_SIZE = 4L;
    private static final long INT_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(int[].class);
    private static final IntegerOffHeapVector INSTANCE = new IntegerOffHeapVector();

    public static IntegerOffHeapVector instance() {
        return INSTANCE;
    }

    public long fromIntArray(int[] arr) {
        int length = arr.length;
        long address = unsafe.allocateMemory(length * INT_SIZE + META_SIZE);
        unsafe.copyMemory(arr, INT_ARRAY_BASE_OFFSET, null, address + META_SIZE, length * INT_SIZE);
        unsafe.putLong(address, INT_SIZE);
        unsafe.putLong(address + BASE_FIELD_SIZE, length);
        unsafe.putLong(address + BASE_FIELD_SIZE + COUNT_FIELD_SIZE, length);
        return address;
    }

    /**
     * If size > Integer.MAX_INTEGER, it copy only Integer.MAX_INTEGER elements
     */
    public int[] toIntArray(long address) {
        long s = size(address);
        int[] arr = new int[(int) s];
        unsafe.copyMemory(null, address + META_SIZE, arr, INT_ARRAY_BASE_OFFSET, arr.length * INT_SIZE);
        return arr;
    }

    /**
     * Return element with given index
     *
     * @param i       element index
     * @param address array pointer (array memory block address)
     */
    public int get(long address, long i) {
        checkIndexBounds(address, i);

        return unsafe.getInt(address + META_SIZE + i * INT_SIZE);
    }

    /**
     * Add element to end of array and increment it's capacity. If capacity > max, array grow it's capacity and max size
     *
     * @param address array pointer (array memory block address)
     * @param t       object to add
     * @return new address
     */
    public long add(long address, int t) {
        long next = size(address);
        long addr = checkBounds(address, next);

        unsafe.putInt(addr + META_SIZE + next * INT_SIZE, t);
        incrementSize(addr);

        return addr;
    }

    public boolean contains(long address, int t) {
        return indexOf(address, t) >= 0;
    }

    public Iterator<Integer> iterator(long address) {
        return new Iter(address);
    }

    public void forEach(long address, Consumer<Integer> consumer) {
        iterator(address).forEachRemaining(consumer);
    }

    public long indexOf(long address, int t) {
        long size = size(address);
        long lastAddress = address + META_SIZE;
        for(long i = 0; i < size; i++) {
            int element = unsafe.getInt(lastAddress);
            if(Integer.compare(element, t) == 0) {
                return i;
            }
            lastAddress += INT_SIZE;
        }
        return -1;
    }

    public long lastIndexOf(long address, int t) {
        long size = size(address);
        long addr = address + META_SIZE;
        for(long i = size - 1; i >= 0; i--) {
            int element = unsafe.getInt(addr + i * INT_SIZE);
            if(Integer.compare(element, t) == 0) {
                return i;
            }
        }
        return -1;
    }

    public int set(long address, long i, int t) {
        checkIndexBounds(address, i);

        int last = unsafe.getInt(address + META_SIZE + i * INT_SIZE);
        unsafe.putInt(address + META_SIZE + i * INT_SIZE, t);
        return last;
    }

    public boolean remove(long address, int obj) {
        long index = indexOf(address, obj);
        if(index >= 0) {
            unsafe.copyMemory(address + META_SIZE + INT_SIZE * (index + 1), address + META_SIZE + INT_SIZE * index, INT_SIZE * (size(address) - index));
            decrementSize(address);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected long getElementSize() {
        return INT_SIZE;
    }

    private class Iter implements Iterator<Integer> {
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
        public Integer next() {
            if(!hasNext()) {
                return null;
            }
            int res = get(address, pos);
            pos++;
            return res;
        }
    }
}
