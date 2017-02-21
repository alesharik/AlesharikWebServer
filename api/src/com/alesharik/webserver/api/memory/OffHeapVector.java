package com.alesharik.webserver.api.memory;

import one.nio.util.JavaInternals;
import sun.misc.Unsafe;

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
public abstract class OffHeapVector<T> {
    protected static final int DEFAULT_INITIAL_COUNT = 16;
    /**
     * <code>newCapacity = oldCapacity + RESIZE_DELTA</code>
     */
    protected static final int RESIZE_DELTA = 20;

    protected static final Unsafe unsafe = JavaInternals.getUnsafe();

    /**
     * Size - <code>sizeof(long) == 8</code><br>
     * Hold <code>sizeof(T)</code>
     */
    protected static final long BASE_FIELD_SIZE = 8L;
    /**
     * Size - <code>sizeof(long) == 8</code><br>
     * Hold array size
     */
    protected static final long COUNT_FIELD_SIZE = 8L;
    /**
     * Size - <code>sizeof(long) == 8</code><br>
     * Hold array max size
     */
    protected static final long MAX_FIELD_SIZE = 8L;
    /**
     * Size - <code>((sizeof(long) == 8) * 3) == 24</code><br>
     * All meta information size
     */
    protected static final long META_SIZE = COUNT_FIELD_SIZE + BASE_FIELD_SIZE + MAX_FIELD_SIZE;

    /**
     * Initial size of all arrays after {@link #allocate()}
     */
    protected final long initialCount;

    public OffHeapVector() {
        this(DEFAULT_INITIAL_COUNT);
    }

    /**
     * Created array initial size
     */
    public OffHeapVector(long initialCount) {
        this.initialCount = initialCount;
    }

    /**
     * Allocate offHeap memory for array. YOU MUST DO {@link #free(long)} BEFORE CLEAN YOUR OBJECT BY GARBAGE COLLECTOR
     *
     * @return memory address
     */
    public long allocate() {
        long address = unsafe.allocateMemory(META_SIZE + (initialCount * getElementSize())); //malloc
        unsafe.putLong(address, getElementSize()); //put BASE(element size)
        unsafe.putLong(address + BASE_FIELD_SIZE, 0L); //put COUNT(array size) == 0
        unsafe.putLong(address + BASE_FIELD_SIZE + COUNT_FIELD_SIZE, initialCount); //put MAX(max size) == initialCount
        return address;
    }

    /**
     * Delete memory region
     *
     * @param address array pointer (array memory block address)
     */
    public void free(long address) {
        unsafe.freeMemory(address);
    }

    /**
     * Return array size
     *
     * @param address array pointer (array memory block address)
     */
    public long size(long address) {
        return unsafe.getLong(address + BASE_FIELD_SIZE);
    }

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
        long max = getMax(address);
        long next = size(address);
        if(next >= max) {
            address = resize(address, max + RESIZE_DELTA);
        }
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

    public boolean isEmpty(long address) {
        return size(address) == 0;
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

    public void remove(long address, long i) {
        long count = size(address);
        if(i < 0) {
            throw new IllegalArgumentException();
        }
        if(i >= count) {
            throw new ArrayIndexOutOfBoundsException();
        }
//        unsafe.copyMemory(address + META_SIZE + getElementSize() * i, address + META_SIZE + getElementSize() * (i + 1), (count - i) * getElementSize());
//        unsafe.putLong(address + BASE_FIELD_SIZE, count - 1);
        unsafe.copyMemory(address + META_SIZE + getElementSize() * (i + 1), address + META_SIZE + getElementSize() * i, getElementSize() * (count - i));
        decrementSize(address);
    }

    public void remove(long address, T obj) {
        long index = indexOf(address, obj);
        if(index >= 0) {
            remove(address, index);
        }
    }

    /**
     * Free all unused memory
     *
     * @param address array pointer (array memory block address)
     * @return new address
     */
    public long shrink(long address) {
        long size = size(address);
        if(getMax(address) > size) {
            address = unsafe.reallocateMemory(address, META_SIZE + getElementSize() * size);
            setMax(address, size);
        }
        return address;
    }

    private long resize(long address, long elementCount) {
        address = unsafe.reallocateMemory(address, META_SIZE + elementCount * getElementSize());
        setMax(address, elementCount);
        return address;
    }

    /**
     * Return maximum array size
     *
     * @param address array pointer (memory block address)
     */
    protected final long getMax(long address) {
        return unsafe.getLong(address + BASE_FIELD_SIZE + COUNT_FIELD_SIZE);
    }

    /**
     * Set maximum array size
     *
     * @param address array pointer (array memory block address)
     * @param max     new maximum array size
     */
    protected final void setMax(long address, long max) {
        unsafe.putLong(address + BASE_FIELD_SIZE + COUNT_FIELD_SIZE, max);
    }

    /**
     * Increment array size
     *
     * @param address array pointer (array memory block address)
     */
    protected final void incrementSize(long address) {
        unsafe.putLong(address + BASE_FIELD_SIZE, size(address) + 1);
    }

    /**
     * Decrement array size
     *
     * @param address array pointer (array memory block address)
     */
    protected final void decrementSize(long address) {
        unsafe.putLong(address + BASE_FIELD_SIZE, size(address) - 1);
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
