package com.alesharik.webserver.api.memory.impl;

import com.alesharik.webserver.api.memory.OffHeapVectorBase;

import java.util.Iterator;
import java.util.function.Consumer;

public class IntegerOffHeapVector extends OffHeapVectorBase {
    private static final long INT_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(int[].class);
    private static final IntegerOffHeapVector INSTANCE = new IntegerOffHeapVector();

    public static IntegerOffHeapVector instance() {
        return INSTANCE;
    }

    public long fromIntArray(int[] arr) {
        int length = arr.length;
        long address = unsafe.allocateMemory(length * 4L + META_SIZE);
        unsafe.copyMemory(arr, INT_ARRAY_BASE_OFFSET, null, address + META_SIZE, length * 4L);
        unsafe.putLong(address, 4L);
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
        unsafe.copyMemory(null, address + META_SIZE, arr, INT_ARRAY_BASE_OFFSET, arr.length * 4L);
        return arr;
    }

    /**
     * Return element with given index
     *
     * @param i       element index
     * @param address array pointer (array memory block address)
     */
    public int get(long address, long i) {
        if(i < 0) {
            throw new IllegalArgumentException();
        }

        long count = size(address);
        if(i >= count) {
            throw new ArrayIndexOutOfBoundsException();
        }

        return unsafe.getInt(address + META_SIZE + i * getElementSize());
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
        address = checkBounds(address, next);

        unsafe.putInt(address + META_SIZE + next * getElementSize(), t);
        incrementSize(address);

        return address;
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
            lastAddress += getElementSize();
        }
        return -1;
    }

    public long lastIndexOf(long address, int t) {
        long size = size(address);
        long lastAddress = address + META_SIZE;
        for(long i = size - 1; i >= 0; i++) {
            int element = unsafe.getInt(lastAddress);
            if(Integer.compare(element, t) == 0) {
                return i;
            }
            lastAddress += getElementSize();
        }
        return -1;
    }

    public int set(long address, int t, long i) {
        checkIndexBounds(address, i);

        int last = unsafe.getInt(address + META_SIZE + i * getElementSize());
        unsafe.putInt(address + META_SIZE + i * getElementSize(), t);
        return last;
    }

    public void remove(long address, int obj) {
        long index = indexOf(address, obj);
        if(index >= 0) {
            remove(address, index);
        }
    }

    @Override
    protected long getElementSize() {
        return 4L; //sizeof(int)
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
