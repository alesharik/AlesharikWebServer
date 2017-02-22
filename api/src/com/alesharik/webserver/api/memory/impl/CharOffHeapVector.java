package com.alesharik.webserver.api.memory.impl;

import com.alesharik.webserver.api.memory.OffHeapVectorBase;

import java.util.Iterator;
import java.util.function.Consumer;

public class CharOffHeapVector extends OffHeapVectorBase {
    private static final long CHAR_SIZE = 2L; //sizeof(char)
    private static final long CHAR_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(char[].class);
    private static final CharOffHeapVector INSTANCE = new CharOffHeapVector();

    public static CharOffHeapVector instance() {
        return INSTANCE;
    }

    public long fromCharArray(char[] arr) {
        int length = arr.length;
        long address = unsafe.allocateMemory(length * CHAR_SIZE + META_SIZE);
        unsafe.copyMemory(arr, CHAR_ARRAY_BASE_OFFSET, null, address + META_SIZE, length * CHAR_SIZE);
        unsafe.putLong(address, CHAR_SIZE);
        unsafe.putLong(address + BASE_FIELD_SIZE + COUNT_FIELD_SIZE, length);
        unsafe.putLong(address + BASE_FIELD_SIZE, length);
        return address;
    }

    /**
     * If size > Integer.MAX_INTEGER, it copy only Integer.MAX_INTEGER elements
     */
    public char[] toCharArray(long address) {
        long s = size(address);
        char[] arr = new char[(int) s];
        unsafe.copyMemory(null, address + META_SIZE, arr, CHAR_ARRAY_BASE_OFFSET, arr.length * CHAR_SIZE);
        return arr;
    }

    /**
     * Return element with given index
     *
     * @param i       element index
     * @param address array pointer (array memory block address)
     */
    public char get(long address, long i) {
        if(i < 0) {
            throw new IllegalArgumentException();
        }

        long count = size(address);
        if(i >= count) {
            throw new ArrayIndexOutOfBoundsException();
        }

        return unsafe.getChar(address + META_SIZE + i * getElementSize());
    }

    /**
     * Add element to end of array and increment it's capacity. If capacity > max, array grow it's capacity and max size
     *
     * @param address array pointer (array memory block address)
     * @param t       object to add
     * @return new address
     */
    public long add(long address, char t) {
        long next = size(address);
        address = checkBounds(address, next);

        unsafe.putChar(address + META_SIZE + next * getElementSize(), t);
        incrementSize(address);

        return address;
    }

    public boolean contains(long address, char t) {
        return indexOf(address, t) >= 0;
    }

    public Iterator<Character> iterator(long address) {
        return new Iter(address);
    }

    public void forEach(long address, Consumer<Character> consumer) {
        iterator(address).forEachRemaining(consumer);
    }

    public long indexOf(long address, char t) {
        long size = size(address);
        long lastAddress = address + META_SIZE;
        for(long i = 0; i < size; i++) {
            char element = unsafe.getChar(lastAddress);
            if(Character.compare(element, t) == 0) {
                return i;
            }
            lastAddress += getElementSize();
        }
        return -1;
    }

    public long lastIndexOf(long address, char t) {
        long size = size(address);
        long lastAddress = address + META_SIZE;
        for(long i = size - 1; i >= 0; i++) {
            char element = unsafe.getChar(lastAddress);
            if(Character.compare(element, t) == 0) {
                return i;
            }
            lastAddress += getElementSize();
        }
        return -1;
    }

    public char set(long address, char t, long i) {
        checkIndexBounds(address, i);

        char last = unsafe.getChar(address + META_SIZE + i * getElementSize());
        unsafe.putChar(address + META_SIZE + i * getElementSize(), t);
        return last;
    }

    public void remove(long address, char obj) {
        long index = indexOf(address, obj);
        if(index >= 0) {
            remove(address, index);
        }
    }

    @Override
    protected long getElementSize() {
        return 8L; //sizeof(char)
    }

    private class Iter implements Iterator<Character> {
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
        public Character next() {
            if(!hasNext()) {
                return null;
            }
            char res = get(address, pos);
            pos++;
            return res;
        }
    }
}
