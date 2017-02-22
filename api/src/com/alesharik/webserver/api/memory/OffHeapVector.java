package com.alesharik.webserver.api.memory;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * ┌────────────┬─────────────┬───────────┬──────┐
 * │ BASE(long) │ COUNT(long) │ MAX(long) │ DATA │
 * └────────────┴─────────────┴───────────┴──────┘
 *
 * @implNote If <code>T</code> can be converted to long, you need write overloaded <code>remove</code> method with your type instead <code>T</code>
 * and call {@link #remove(long, Object)} method
 */
@SuppressWarnings("WeakerAccess")
@NotThreadSafe
public abstract class OffHeapVector<T> extends OffHeapVectorBase {
    /**
     * Return element with given index
     *
     * @param i       element index
     * @param address array pointer (array memory block address)
     */
    public T get(long address, long i) {
        if(i < 0) {
            throw new IllegalArgumentException();
        }

        long count = size(address);
        if(i >= count) {
            throw new ArrayIndexOutOfBoundsException();
        }

        return getUnsafe(address + META_SIZE + i * getElementSize());
    }

    /**
     * Add element to end of array and increment it's capacity. If capacity > max, array grow it's capacity and max size
     *
     * @param address array pointer (array memory block address)
     * @param t       object to add
     * @return new address
     */
    public long add(long address, T t) {
        long next = size(address);
        address = checkBounds(address, next);

        setUnsafe(address + META_SIZE + next * getElementSize(), t);
        incrementSize(address);

        return address;
    }

    public boolean contains(long address, T t) {
        return indexOf(address, t) >= 0;
    }

    public Iterator<T> iterator(long address) {
        return new Iter(address);
    }

    public void forEach(long address, Consumer<T> consumer) {
        iterator(address).forEachRemaining(consumer);
    }

    public long indexOf(long address, T t) {
        long size = size(address);
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
        long size = size(address);
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
        long count = size(address);
        if(i < 0) {
            throw new IllegalArgumentException();
        }
        if(i >= count) {
            throw new ArrayIndexOutOfBoundsException();
        }

        T last = getUnsafe(address + META_SIZE + i * getElementSize());
        setUnsafe(address + META_SIZE + i * getElementSize(), t);
        return last;
    }

    public void remove(long address, T obj) {
        long index = indexOf(address, obj);
        if(index >= 0) {
            remove(address, index);
        }
    }

    /**
     * Element size in bytes
     */
    protected abstract long getElementSize();

    /**
     * Return element from it's address
     *
     * @param address memory address + META_SIZE + i * getElementSize()
     */
    protected abstract T getUnsafe(long address);

    protected abstract void setUnsafe(long address, T t);

    protected abstract boolean elementEquals(T t1, T t2);

    private class Iter implements Iterator<T> {
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
        public T next() {
            if(!hasNext()) {
                return null;
            }
            T res = get(address, pos);
            pos++;
            return res;
        }
    }
}
