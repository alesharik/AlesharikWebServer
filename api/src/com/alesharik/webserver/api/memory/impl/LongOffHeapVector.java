package com.alesharik.webserver.api.memory.impl;

import com.alesharik.webserver.api.memory.OffHeapVectorBase;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * You need to use {@link #remove(long, Long)} with {@link Long} type for long object removal
 */
public final class LongOffHeapVector extends OffHeapVectorBase {
    private static final long LONG_SIZE = 8L; //sizeof(long)
    private static final long LONG_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(long[].class);
    private static final LongOffHeapVector INSTANCE = new LongOffHeapVector();

    public static LongOffHeapVector instance() {
        return INSTANCE;
    }

    public long fromLongArray(long[] arr) {
        int length = arr.length;
        long address = unsafe.allocateMemory(length * LONG_SIZE + META_SIZE);
        unsafe.copyMemory(arr, LONG_ARRAY_BASE_OFFSET, null, address + META_SIZE, length * LONG_SIZE);
        unsafe.putLong(address, LONG_SIZE);
        unsafe.putLong(address + BASE_FIELD_SIZE, length);
        unsafe.putLong(address + BASE_FIELD_SIZE + COUNT_FIELD_SIZE, length);
        return address;
    }

    /**
     * If size > Integer.MAX_INTEGER, it copy only Integer.MAX_INTEGER elements
     */
    public long[] toLongArray(long address) {
        long s = size(address);
        long[] arr = new long[(int) s];
        unsafe.copyMemory(null, address + META_SIZE, arr, LONG_ARRAY_BASE_OFFSET, arr.length * LONG_SIZE);
        return arr;
    }

    /**
     * Return element with given index
     *
     * @param i       element index
     * @param address array pointer (array memory block address)
     */
    public long get(long address, long i) {
        checkIndexBounds(address, i);

        return unsafe.getLong(address + META_SIZE + i * getElementSize());
    }

    /**
     * Add element to end of array and increment it's capacity. If capacity > max, array grow it's capacity and max size
     *
     * @param address array pointer (array memory block address)
     * @param t       object to add
     * @return new address
     */
    public long add(long address, long t) {
        long next = size(address);
        address = checkBounds(address, next);

        unsafe.putLong(address + META_SIZE + next * getElementSize(), t);
        incrementSize(address);

        return address;
    }

    public boolean contains(long address, long t) {
        return indexOf(address, t) >= 0;
    }

    public Iterator<Long> iterator(long address) {
        return new Iter(address);
    }

    public void forEach(long address, Consumer<Long> consumer) {
        iterator(address).forEachRemaining(consumer);
    }

    public long indexOf(long address, long t) {
        long size = size(address);
        long lastAddress = address + META_SIZE;
        for(long i = 0; i < size; i++) {
            long element = unsafe.getLong(lastAddress);
            if(Long.compare(element, t) == 0) {
                return i;
            }
            lastAddress += LONG_SIZE;
        }
        return -1;
    }

    public long lastIndexOf(long address, long t) {
        long size = size(address);
        long addr = address + META_SIZE;
        for(long i = size - 1; i >= 0; i--) {
            long element = unsafe.getLong(addr + i * LONG_SIZE);
            if(Long.compare(element, t) == 0) {
                return i;
            }
        }
        return -1;
    }

    public long set(long address, long i, long t) {
        checkIndexBounds(address, i);

        long last = unsafe.getLong(address + META_SIZE + i * getElementSize());
        unsafe.putLong(address + META_SIZE + i * getElementSize(), t);
        return last;
    }

    public boolean remove(long address, Long obj) {
        long index = indexOf(address, obj);
        if(index >= 0) {
            unsafe.copyMemory(address + META_SIZE + LONG_SIZE * (index + 1), address + META_SIZE + LONG_SIZE * index, LONG_SIZE * (size(address) - index));
            decrementSize(address);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected long getElementSize() {
        return LONG_SIZE; //sizeof(short)
    }

    private class Iter implements Iterator<Long> {
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
        public Long next() {
            if(!hasNext()) {
                return null;
            }
            long res = get(address, pos);
            pos++;
            return res;
        }
    }
}
