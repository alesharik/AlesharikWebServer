package com.alesharik.webserver.api.memory;

import one.nio.util.JavaInternals;
import sun.misc.Unsafe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * ┌────────────┬─────────────┬───────────┬──────┐
 * │ BASE(long) │ COUNT(long) │ MAX(long) │ DATA │
 * └────────────┴─────────────┴───────────┴──────┘
 */
@SuppressWarnings("WeakerAccess")
public abstract class Array<T> {
    private static final int DEFAULT_INITIAL_COUNT = 16;
    private static final int ADD_ELEMENT_COUNT = 20;

    protected static final Unsafe unsafe = JavaInternals.getUnsafe();
    protected static final long COUNT_FIELD_SIZE = 8L; //count long
    protected static final long BASE_SIZE = 8L;//sizeof long
    protected static final long MAX_SIZE = 8L; //sizeof long
    protected static final long META_SIZE = COUNT_FIELD_SIZE + BASE_SIZE + MAX_SIZE;

    protected final long initialCount;

    public Array() {
        this(DEFAULT_INITIAL_COUNT);
    }

    public Array(long initialCount) {
        this.initialCount = initialCount;
    }

    public long allocate() {
        long address = unsafe.allocateMemory(initialCount * getElementSize());
        unsafe.setMemory(address, META_SIZE, (byte) 0);
        return address;
    }

    public void free(long address) {
        unsafe.freeMemory(address);
    }

    public long getSize(long address) {
        return META_SIZE + count(address) * getElementSize();
    }

    public long count(long address) {
        return unsafe.getLong(address + BASE_SIZE);
    }

    public T get(long address, long i) {
        long count = count(address);
        if(i >= count) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return getUnsafe(address + META_SIZE + i * getElementSize());
    }

    public long add(long address, T t) {
        long max = unsafe.getLong(address + BASE_SIZE + COUNT_FIELD_SIZE);
        long next = count(address);
        if(next > max) {
            resize(address, max + ADD_ELEMENT_COUNT);
        }
        setUnsafe(address + META_SIZE + next * getElementSize(), t);
        unsafe.putLong(address + BASE_SIZE, next + 1);
        return next;
    }

    public boolean contains(long address, T t) {
        long size = count(address);
        long lastAddress = address + META_SIZE;
        for(long i = 0; i < size; i++) {
            T element = getUnsafe(lastAddress);
            if(elementEquals(element, t)) {
                return true;
            }
            lastAddress += getElementSize();
        }
        return false;
    }

    public Iterator<T> iterator(long address) {
        ArrayList<T> list = new ArrayList<>();
        forEach(address, list::add);
        return list.iterator();
    }

    public void forEach(long address, Consumer<T> consumer) {
        long size = count(address);
        long lastAddress = address + META_SIZE;
        for(long i = 0; i < size; i++) {
            T element = getUnsafe(lastAddress);
            consumer.accept(element);
            lastAddress += getElementSize();
        }
    }

    public boolean isEmpty(long address) {
        return unsafe.getLong(address + BASE_SIZE) == 0;
    }

    public long indexOf(long address, T t) {
        long size = count(address);
        long lastAddress = address + META_SIZE;
        for(long i = 0; i < size; i++) {
            T element = getUnsafe(lastAddress);
            if(elementEquals(element, t)) {
                return i;
            }
            lastAddress += getElementSize();
        }
        return -1;
    }

    public long lastIndexOf(long address, T t) {
        long size = count(address);
        long lastAddress = address + META_SIZE;
        for(long i = size - 1; i >= 0; i++) {
            T element = getUnsafe(lastAddress);
            if(elementEquals(element, t)) {
                return i;
            }
            lastAddress += getElementSize();
        }
        return -1;
    }

    public T set(long address, T t, long i) {
        long count = count(address);
        if(i >= count) {
            throw new ArrayIndexOutOfBoundsException();
        }
        T last = getUnsafe(address + META_SIZE + i * getElementSize());
        setUnsafe(address + META_SIZE + i * getElementSize(), t);
        return last;
    }

    private void resize(long address, long elementCount) {
        unsafe.reallocateMemory(address, META_SIZE + elementCount * getElementSize());
        unsafe.putLong(address + BASE_SIZE + COUNT_FIELD_SIZE, elementCount);
    }

    protected abstract long getElementSize();

    protected abstract T getUnsafe(long address);

    protected abstract void setUnsafe(long address, T t);

    protected abstract boolean elementEquals(T t1, T t2);
}
