package com.alesharik.webserver.api.memory.impl;

import com.alesharik.webserver.api.memory.OffHeapVectorBase;

import java.util.Iterator;
import java.util.function.Consumer;

public final class CharOffHeapVector extends OffHeapVectorBase {
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
        checkIndexBounds(address, i);

        return unsafe.getChar(address + META_SIZE + i * 2);
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
        long addr = checkBounds(address, next);

        unsafe.putChar(addr + META_SIZE + next * 2, t);
        incrementSize(addr);

        return addr;
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
            lastAddress += 2;
        }
        return -1;
    }

    public long lastIndexOf(long address, char t) {
        long size = size(address);
        long addr = address + META_SIZE;
        for(long i = size - 1; i >= 0; i--) {
            char element = unsafe.getChar(addr + i * 2);
            if(Character.compare(element, t) == 0) {
                return i;
            }
        }
        return -1;
    }

    public char set(long address, long i, char t) {
        checkIndexBounds(address, i);

        char last = unsafe.getChar(address + META_SIZE + i * 2);
        unsafe.putChar(address + META_SIZE + i * 2, t);
        return last;
    }

    public boolean remove(long address, char obj) {
        long index = indexOf(address, obj);
        if(index >= 0) {
            unsafe.copyMemory(address + META_SIZE + 2 * (index + 1), address + META_SIZE + 2 * index, 2 * (size(address) - index));
            decrementSize(address);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected long getElementSize() {
        return 2L; //sizeof(char)
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
