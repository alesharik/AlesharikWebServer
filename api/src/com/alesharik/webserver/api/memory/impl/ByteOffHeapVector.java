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

public final class ByteOffHeapVector extends OffHeapVectorBase {
    private static final long BYTE_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(byte[].class);
    private static final ByteOffHeapVector INSTANCE = new ByteOffHeapVector();

    public static ByteOffHeapVector instance() {
        return INSTANCE;
    }

    public long fromByteArray(byte[] arr) {
        int length = arr.length;
        long address = unsafe.allocateMemory(length + META_SIZE);
        unsafe.copyMemory(arr, BYTE_ARRAY_BASE_OFFSET, null, address + META_SIZE, length);
        unsafe.putLong(address, 1);
        unsafe.putLong(address + BASE_FIELD_SIZE + COUNT_FIELD_SIZE, length);
        unsafe.putLong(address + BASE_FIELD_SIZE, length);
        return address;
    }

    /**
     * If size > Integer.MAX_INTEGER, it copy only Integer.MAX_INTEGER elements
     */
    public byte[] toByteArray(long address) {
        long s = size(address);
        byte[] arr = new byte[(int) s];
        unsafe.copyMemory(null, address + META_SIZE, arr, BYTE_ARRAY_BASE_OFFSET, arr.length);
        return arr;
    }

    public byte[] cut(long address, int size) {
        long s = size(address);
        if(s < size)
            throw new IllegalArgumentException();
        byte[] arr = new byte[size];
        unsafe.copyMemory(null, address + META_SIZE, arr, BYTE_ARRAY_BASE_OFFSET, size);
        unsafe.putLong(address + BASE_FIELD_SIZE, s - size);
        unsafe.copyMemory(null, address + META_SIZE + size, null, address + META_SIZE, s - size);
        return arr;
    }

    /**
     * Return element with given index
     *
     * @param i       element index
     * @param address array pointer (array memory block address)
     */
    public byte get(long address, long i) {
        checkIndexBounds(address, i);

        return unsafe.getByte(address + META_SIZE + i);
    }

    /**
     * Add element to end of array and increment it's capacity. If capacity > max, array grow it's capacity and max size
     *
     * @param address array pointer (array memory block address)
     * @param t       object to add
     * @return new address
     */
    public long add(long address, byte t) {
        long next = size(address);
        long addr = checkBounds(address, next);

        unsafe.putByte(addr + META_SIZE + next, t);
        incrementSize(addr);

        return addr;
    }

    public boolean contains(long address, byte t) {
        return indexOf(address, t) >= 0;
    }

    public Iterator<Byte> iterator(long address) {
        return new Iter(address);
    }

    public void forEach(long address, Consumer<Byte> consumer) {
        iterator(address).forEachRemaining(consumer);
    }

    public long indexOf(long address, byte t) {
        long size = size(address);
        long lastAddress = address + META_SIZE;
        for(long i = 0; i < size; i++) {
            byte element = unsafe.getByte(lastAddress);
            if(Byte.compare(element, t) == 0) {
                return i;
            }
            lastAddress++;
        }
        return -1;
    }

    public long lastIndexOf(long address, byte t) {
        long size = size(address);
        long addr = address + META_SIZE;
        for(long i = size - 1; i >= 0; i--) {
            byte element = unsafe.getByte(addr + i);
            if(Byte.compare(element, t) == 0) {
                return i;
            }
        }
        return -1;
    }

    public byte set(long address, long i, byte t) {
        checkIndexBounds(address, i);

        byte last = unsafe.getByte(address + META_SIZE + i);
        unsafe.putByte(address + META_SIZE + i, t);
        return last;
    }

    public boolean remove(long address, byte obj) {
        long index = indexOf(address, obj);
        if(index >= 0) {
            unsafe.copyMemory(address + META_SIZE + getElementSize() * (index + 1), address + META_SIZE + getElementSize() * index, getElementSize() * (size(address) - index));
            decrementSize(address);
            return true;
        } else {
            return false;
        }
    }

    public long write(long arr, byte[] buf) {
        return write(arr, buf, 0, buf.length);
    }

    public long write(long arr, byte[] buf, int off, int length) {
        if(off < 0 || length < 0)
            throw new IllegalArgumentException();

        long reqLen = length - off;
        if(buf.length < reqLen)
            throw new IllegalArgumentException();

        long len = size(arr);
        long addr = checkBounds(arr, len + reqLen);
        unsafe.copyMemory(buf, BYTE_ARRAY_BASE_OFFSET + off, null, addr + META_SIZE + len, reqLen);
        unsafe.putLong(addr + BASE_FIELD_SIZE, len + reqLen);
        return addr;
    }

    public void clear(long addr) {
        unsafe.putLong(addr + BASE_FIELD_SIZE, 0);//Set size
    }

    @Override
    protected long getElementSize() {
        return 1L; //sizeof(byte)
    }

    private class Iter implements Iterator<Byte> {
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
        public Byte next() {
            if(!hasNext()) {
                return null;
            }
            byte res = get(address, pos);
            pos++;
            return res;
        }
    }
}
