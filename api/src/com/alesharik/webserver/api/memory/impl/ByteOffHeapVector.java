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

    /**
     * Return element with given index
     *
     * @param i       element index
     * @param address array pointer (array memory block address)
     */
    public byte get(long address, long i) {
        if(i < 0) {
            throw new IllegalArgumentException();
        }

        long count = size(address);
        if(i >= count) {
            throw new ArrayIndexOutOfBoundsException();
        }

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
        address = checkBounds(address, next);

        unsafe.putByte(address + META_SIZE + next, t);
        incrementSize(address);

        return address;
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
        long lastAddress = address + META_SIZE;
        for(long i = size - 1; i >= 0; i++) {
            byte element = unsafe.getByte(lastAddress);
            if(Byte.compare(element, t) == 0) {
                return i;
            }
            lastAddress++;
        }
        return -1;
    }

    public byte set(long address, byte t, long i) {
        checkIndexBounds(address, i);

        byte last = unsafe.getByte(address + META_SIZE + i);
        unsafe.putByte(address + META_SIZE + i, t);
        return last;
    }

    public void remove(long address, byte obj) {
        long index = indexOf(address, obj);
        if(index >= 0) {
            remove(address, index);
        }
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
