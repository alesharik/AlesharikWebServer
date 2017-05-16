package com.alesharik.webserver.api.memory.impl;

import com.alesharik.webserver.api.memory.OffHeapVectorBase;

import java.util.Iterator;
import java.util.function.Consumer;

public final class ShortOffHeapVector extends OffHeapVectorBase {
    private static final long SHORT_SIZE = 2L; //sizeof(long)
    private static final long SHORT_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(short[].class);
    private static final ShortOffHeapVector INSTANCE = new ShortOffHeapVector();

    public static ShortOffHeapVector instance() {
        return INSTANCE;
    }

    public long fromShortArray(short[] arr) {
        int length = arr.length;
        long address = unsafe.allocateMemory(length * SHORT_SIZE + META_SIZE);
        unsafe.copyMemory(arr, SHORT_ARRAY_BASE_OFFSET, null, address + META_SIZE, length * SHORT_SIZE);
        unsafe.putLong(address, SHORT_SIZE);
        unsafe.putLong(address + BASE_FIELD_SIZE, length);
        unsafe.putLong(address + BASE_FIELD_SIZE + COUNT_FIELD_SIZE, length);
        return address;
    }

    /**
     * If size > Integer.MAX_INTEGER, it copy only Integer.MAX_INTEGER elements
     */
    public short[] toShortArray(long address) {
        long s = size(address);
        short[] arr = new short[(int) s];
        unsafe.copyMemory(null, address + META_SIZE, arr, SHORT_ARRAY_BASE_OFFSET, arr.length * SHORT_SIZE);
        return arr;
    }

    /**
     * Return element with given index
     *
     * @param i       element index
     * @param address array pointer (array memory block address)
     */
    public short get(long address, long i) {
        checkIndexBounds(address, i);

        return unsafe.getShort(address + META_SIZE + i * SHORT_SIZE);
    }

    /**
     * Add element to end of array and increment it's capacity. If capacity > max, array grow it's capacity and max size
     *
     * @param address array pointer (array memory block address)
     * @param t       object to add
     * @return new address
     */
    public long add(long address, short t) {
        long next = size(address);
        address = checkBounds(address, next);

        unsafe.putShort(address + META_SIZE + next * getElementSize(), t);
        incrementSize(address);

        return address;
    }

    public boolean contains(long address, short t) {
        return indexOf(address, t) >= 0;
    }

    public Iterator<Short> iterator(long address) {
        return new Iter(address);
    }

    public void forEach(long address, Consumer<Short> consumer) {
        iterator(address).forEachRemaining(consumer);
    }

    public long indexOf(long address, short t) {
        long size = size(address);
        long lastAddress = address + META_SIZE;
        for(long i = 0; i < size; i++) {
            short element = unsafe.getShort(lastAddress);
            if(Short.compare(element, t) == 0) {
                return i;
            }
            lastAddress += SHORT_SIZE;
        }
        return -1;
    }

    public long lastIndexOf(long address, short t) {
        long size = size(address);
        long addr = address + META_SIZE;
        for(long i = size - 1; i >= 0; i--) {
            short element = unsafe.getShort(addr + i * SHORT_SIZE);
            if(Short.compare(element, t) == 0) {
                return i;
            }
        }
        return -1;
    }

    public short set(long address, long i, short t) {
        checkIndexBounds(address, i);

        short last = unsafe.getShort(address + META_SIZE + i * getElementSize());
        unsafe.putShort(address + META_SIZE + i * getElementSize(), t);
        return last;
    }

    public boolean remove(long address, short obj) {
        long index = indexOf(address, obj);
        if(index >= 0) {
            unsafe.copyMemory(address + META_SIZE + SHORT_SIZE * (index + 1), address + META_SIZE + SHORT_SIZE * index, SHORT_SIZE * (size(address) - index));
            decrementSize(address);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected long getElementSize() {
        return SHORT_SIZE; //sizeof(short)
    }

    private class Iter implements Iterator<Short> {
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
        public Short next() {
            if(!hasNext()) {
                return null;
            }
            short res = get(address, pos);
            pos++;
            return res;
        }
    }
}
