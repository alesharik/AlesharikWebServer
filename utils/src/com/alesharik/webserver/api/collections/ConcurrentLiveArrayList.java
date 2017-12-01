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

package com.alesharik.webserver.api.collections;

import com.alesharik.webserver.api.ticking.OneThreadTickingPool;
import com.alesharik.webserver.api.ticking.Tickable;
import com.alesharik.webserver.api.ticking.TickingPool;
import com.alesharik.webserver.exception.error.BadImplementationError;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * This list store values with specific period(life time). It update values from {@link TickingPool}
 *
 * @param <V> element type
 */
@ThreadSafe
public class ConcurrentLiveArrayList<V> implements Tickable, Cloneable, Serializable, List<V>, RandomAccess, Stoppable {
    protected static final long DEFAULT_LIFE_TIME = TimeUnit.MINUTES.toMillis(1);
    protected static final long DEFAULT_DELAY = 1000;
    private static final OneThreadTickingPool DEFAULT_POOL = new OneThreadTickingPool();
    private static final int DEFAULT_CAPACITY = 16;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private static final long serialVersionUID = 1861951523878433866L;

    private final AtomicInteger size;
    private transient final StampedLock lock = new StampedLock();
    private final AtomicBoolean started;
    @Setter
    @Getter
    private volatile long defaultPeriod = DEFAULT_LIFE_TIME;
    private Element<V>[] elements;
    private volatile long delay;
    @Getter
    private volatile TickingPool tickingPool = DEFAULT_POOL;

    public ConcurrentLiveArrayList() {
        this(DEFAULT_DELAY, DEFAULT_CAPACITY);
    }

    public ConcurrentLiveArrayList(int count) {
        this(DEFAULT_DELAY, count);
    }

    public ConcurrentLiveArrayList(long delay) {
        this(delay, DEFAULT_CAPACITY);
    }

    public ConcurrentLiveArrayList(long delay, int count) {
        this(delay, count, true);
    }

    public ConcurrentLiveArrayList(long delay, int count, boolean autoStart) {
        this.elements = new Element[count];
        this.delay = delay;
        this.size = new AtomicInteger(0);
        this.started = new AtomicBoolean(false);
        if(autoStart)
            start();
    }

    @Override
    public boolean add(@Nullable V v) {
        return add(v, defaultPeriod);
    }

    /**
     * @param period live time period
     * @return true if collection was changed
     * @see #add(Object)
     */
    public boolean add(@Nullable V v, long period) {
        if(period < 0)
            throw new IllegalArgumentException();
        long write = lock.writeLock();
        try {
            int i = size.getAndIncrement();
            if(i >= elements.length)
                resize(size.get());
            elements[i] = new Element<>(v, period);
            return true;
        } finally {
            lock.unlockWrite(write);
        }
    }

    @Override
    @Nullable
    public V set(int index, @Nullable V element) {
        long l = lock.readLock();
        try {
            checkRange(index);
            long lockLast = l;
            l = lock.tryConvertToWriteLock(l);
            if(l == 0) {
                lock.unlockRead(lockLast);
                l = lock.writeLock();
                checkRange(index);
            }
            Element<V> elem = elements[index];
            V last = elem.element;
            elem.element = element;
            return last;
        } finally {
            lock.unlock(l);
        }
    }

    @Nullable
    public V set(int index, @Nullable V element, long period) {
        long l = lock.readLock();
        try {
            checkRange(index);
            long lockLast = l;
            l = lock.tryConvertToWriteLock(l);
            if(l == 0) {
                lock.unlockRead(lockLast);
                l = lock.writeLock();
                checkRange(index);
            }
            Element<V> elem = elements[index];
            V last = elem.element;
            elem.element = element;
            elem.period = period;
            return last;
        } finally {
            lock.unlock(l);
        }
    }

    public long setPeriod(int index, long period) {
        long l = lock.readLock();
        try {
            checkRange(index);
            long lockLast = l;
            l = lock.tryConvertToWriteLock(l);
            if(l == 0) {
                lock.unlockRead(lockLast);
                l = lock.writeLock();
                checkRange(index);
            }
            Element<V> elem = elements[index];
            long last = elem.period;
            elem.period = period;
            return last;
        } finally {
            lock.unlock(l);
        }
    }

    public long getPeriod(int index) {
        long l = lock.readLock();
        try {
            checkRange(index);
            return elements[index].period;
        } finally {
            lock.unlockRead(l);
        }
    }

    public long getLiveTime(int index) {
        long l = lock.readLock();
        try {
            checkRange(index);
            return elements[index].liveTime();
        } finally {
            lock.unlockRead(l);
        }
    }

    public void resetTime(int index) {
        long l = lock.readLock();
        try {
            checkRange(index);
            long lockLast = l;
            l = lock.tryConvertToWriteLock(l);
            if(l == 0) {
                lock.unlockRead(lockLast);
                l = lock.writeLock();
                checkRange(index);
            }
            elements[index].reset();
        } finally {
            lock.unlock(l);
        }
    }

    @Override
    public void add(int index, @Nullable V element) {
        add(index, element, defaultPeriod);
    }

    public void add(int index, @Nullable V element, long period) {
        long l = lock.readLock();
        try {
            checkRange(index);
            long lockLast = l;
            l = lock.tryConvertToWriteLock(l);
            if(l == 0) {
                lock.unlockRead(lockLast);
                l = lock.writeLock();
                checkRange(index);
            }
            int i = size.getAndIncrement();
            if(i >= elements.length)
                resize(size.get());
            System.arraycopy(elements, index, elements, index + 1, i - index);
            elements[index] = new Element<>(element, period);
        } finally {
            lock.unlock(l);
        }
    }

    @Override
    @Nullable
    public V remove(int index) {
        long l = lock.readLock();
        try {
            checkRange(index);
            long lockLast = l;
            l = lock.tryConvertToWriteLock(l);
            if(l == 0) {
                lock.unlockRead(lockLast);
                l = lock.writeLock();
                checkRange(index);
            }
            return remove0(index);
        } finally {
            lock.unlock(l);
        }
    }

    protected V remove0(int index) {
        int i = size.getAndDecrement();
        int move = i - index - 1;
        Element<V> element = elements[index];
        if(element == null)
            return null;
        if(move > 0)
            System.arraycopy(elements, index + 1, elements, index, move);
        elements[i - 1] = null;
        return element.element;
    }

    @Override
    public int indexOf(@Nullable Object o) {
        long l = lock.readLock();
        try {
            if(o == null)
                for(int i = 0; i < size.get(); i++) {
                    if(elements[i].element == null)
                        return i;
                }
            else
                for(int i = 0; i < size.get(); i++)
                    if(o.equals(elements[i].element))
                        return i;
            return -1;
        } finally {
            lock.unlockRead(l);
        }
    }

    @Override
    public int lastIndexOf(@Nullable Object o) {
        long l = lock.readLock();
        try {
            if(o == null)
                for(int i = size.get() - 1; i >= 0; i--) {
                    if(elements[i].element == null)
                        return i;
                }
            else
                for(int i = size.get() - 1; i >= 0; i--)
                    if(o.equals(elements[i].element))
                        return i;
            return -1;
        } finally {
            lock.unlockRead(l);
        }
    }

    @Override
    public void clear() {
        long l = lock.writeLock();
        try {
            elements = new Element[size.getAndSet(0)];
        } finally {
            lock.unlockWrite(l);
        }
    }

    @Override
    public boolean addAll(int index, @Nonnull Collection<? extends V> c) {
        return addAll(index, c, defaultPeriod);
    }

    public <T extends V> boolean addAll(int index, @Nonnull Collection<T> c, long period) {
        if(c.size() < 0)
            return false;
        long l = lock.readLock();
        try {
            if(index != 0)
                checkRange(index);
            long lockLast = l;
            l = lock.tryConvertToWriteLock(l);
            if(l == 0) {
                lock.unlockRead(lockLast);
                l = lock.writeLock();
                checkRange(index);
            }
            int nextSize = size.get() + c.size();
            if(elements.length <= nextSize)
                resize(nextSize);
            System.arraycopy(elements, index, elements, index + c.size(), size.get() - index);
            int j = index;
            for(T t : c) {
                elements[j] = new Element<>(t, period);
                j++;
            }
            size.set(nextSize);
        } finally {
            lock.unlock(l);
        }
        return c.size() > 0;
    }

    @Override
    @Nonnull
    public Iterator<V> iterator() {
        return new IteratorImpl();
    }

    @Override
    @Nonnull
    public ListIterator<V> listIterator() {
        return new ListIteratorImpl();
    }

    @Override
    @Nonnull
    public ListIterator<V> listIterator(int index) {
        return new ListIteratorImpl(index);
    }

    @Override
    @Nonnull
    public List<V> subList(int fromIndex, int toIndex) {
        long l = lock.readLock();
        try {
            checkRange(fromIndex);
            checkRange(toIndex - 1);
            if(toIndex - fromIndex <= 0)
                throw new IndexOutOfBoundsException("from: " + fromIndex + ", to: " + toIndex);
            ConcurrentLiveArrayList<V> list = new ConcurrentLiveArrayList<>(delay, toIndex - fromIndex);
            System.arraycopy(elements, fromIndex, list.elements, 0, toIndex - fromIndex);
            list.size.set(toIndex - fromIndex);
            return list;
        } finally {
            lock.unlockRead(l);
        }
    }

    @Override
    public boolean isEmpty() {
        return size.get() == 0;
    }

    @Override
    public boolean contains(@Nullable Object o) {
        long read = lock.readLock();
        try {
            for(Element<V> element : elements) {
                if(element == null)
                    return false;

                if((element.element == null && o == null) || (element.element != null && element.element.equals(o)))
                    return true;
            }
            return false;
        } finally {
            lock.unlockRead(read);
        }
    }

    @Override
    @Nonnull
    public Object[] toArray() {
        long read = lock.readLock();
        try {
            Object[] arr = new Object[size.get()];
            for(int i = 0; i < size.get(); i++)
                arr[i] = elements[i].element;
            return arr;
        } finally {
            lock.unlockRead(read);
        }
    }

    @SuppressWarnings({"unchecked"})
    @Override
    @Nullable
    @Contract("null -> null; !null -> !null")
    public <C> C[] toArray(@Nullable C[] a) {
        if(a == null)
            return null;
        long read = lock.readLock();
        try {
            C[] arr = Arrays.copyOf(a, size.get());
            for(int i = 0; i < size.get(); i++)
                arr[i] = (C) elements[i].element;
            return arr;
        } finally {
            lock.unlockRead(read);
        }
    }

    @Override
    public boolean remove(@Nullable Object o) {
        long l = lock.readLock();
        try {
            for(int i = 0; i < size.get(); i++) {
                Element<V> element = elements[i];
                if(element.element.equals(o)) {
                    long lockLast = l;
                    l = lock.tryConvertToWriteLock(l);
                    if(l == 0) {
                        lock.unlockRead(lockLast);
                        l = lock.writeLock();
                    }
                    remove0(i);
                    return true;
                }
            }
        } finally {
            lock.unlock(l);
        }
        return false;
    }

    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {
        long read = lock.readLock();
        try {
            for(Object o : c) {
                boolean ok = false;
                for(Element<V> element : elements) {
                    if(element == null)
                        return true;
                    if(o.equals(element.element)) {
                        ok = true;
                        break;
                    }
                }
                if(!ok)
                    return false;
            }
            return true;
        } finally {
            lock.unlockRead(read);
        }
    }

    @Override
    public boolean addAll(@Nonnull Collection<? extends V> c) {
        return addAll(0, c);
    }

    public boolean addAll(@Nonnull Collection<? extends V> c, long lifeTime) {
        return addAll(0, c, lifeTime);
    }

    @Override
    public boolean removeAll(@Nonnull Collection<?> c) {
        for(Object o : c)
            if(!remove(o))
                return false;
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean retainAll(@Nonnull Collection<?> c) {
        clear();
        return c.stream().allMatch(o -> add((V) o));
    }

    public boolean retainAll(@Nonnull Collection<? extends V> c, long lifeTime) {
        clear();
        return addAll(c, lifeTime);
    }

    @Override
    @Nonnull
    public String toString() {
        long write = lock.writeLock();
        try {
            return "ConcurrentLiveArrayList{" +
                    "size=" + size +
                    ", started=" + started +
                    ", defaultPeriod=" + defaultPeriod +
                    ", elements=" + Arrays.toString(elements) +
                    ", delay=" + delay +
                    ", tickingPool=" + tickingPool +
                    '}';
        } finally {
            lock.unlockWrite(write);
        }
    }

    @Override
    public void replaceAll(@Nonnull UnaryOperator<V> operator) {
        long write = lock.writeLock();
        try {
            for(Element<V> element : elements) {
                if(element == null)
                    return;
                element.element = operator.apply(element.element);
                element.reset();
            }
        } finally {
            lock.unlockWrite(write);
        }
    }

    /**
     * Reset timing
     *
     * @param operator         the operator
     * @param lifeTimeOperator life time replacer
     */
    public void replaceAll(@Nonnull UnaryOperator<V> operator, @Nonnull UnaryOperator<Long> lifeTimeOperator) {
        long write = lock.writeLock();
        try {
            for(Element<V> element : elements) {
                if(element == null)
                    return;
                element.element = operator.apply(element.element);
                element.period = lifeTimeOperator.apply(element.period);
                element.reset();
            }
        } finally {
            lock.unlockWrite(write);
        }
    }

    @Override
    public void sort(@Nullable Comparator<? super V> c) {
        long write = lock.writeLock();
        try {
            if(c == null) {
                Arrays.sort(elements, (o1, o2) -> {
                    if(o1 == null && o2 == null) return 0;
                    if(o1 == null) return +1;
                    if(o2 == null) return -1;

                    return ((Comparable<V>) o1.element).compareTo(o2.element);
                });
            } else
                Arrays.sort(elements, (o1, o2) -> {
                    if(o1 == null && o2 == null) return 0;
                    if(o1 == null) return +1;
                    if(o2 == null) return -1;

                    return c.compare(o1.element, o2.element);
                });
        } finally {
            lock.unlockWrite(write);
        }
    }

    @Override
    @Nonnull
    public Spliterator<V> spliterator() {
        long read = lock.readLock();
        try {
            return Spliterators.spliterator(iterator(), Spliterator.ORDERED, Spliterator.SIZED);
        } finally {
            lock.unlockRead(read);
        }
    }

    @Override
    public boolean removeIf(@Nonnull Predicate<? super V> filter) {
        long l = lock.writeLock();
        try {
            boolean ok = false;
            for(int i = 0; i < size.get(); i++)
                if(filter.test(elements[i].element)) {
                    remove0(i);
                    i--;
                    ok = true;
                }
            return ok;
        } finally {
            lock.unlockWrite(l);
        }
    }

    @Override
    @Nonnull
    public Stream<V> stream() {
        Stream.Builder<V> ret = Stream.builder();
        forEach(ret);
        return ret.build();
    }

    @Override
    @Nonnull
    public Stream<V> parallelStream() {
        Stream.Builder<V> ret = Stream.builder();
        forEach(ret);
        return ret.build().parallel();
    }

    @Override
    public void forEach(@Nonnull Consumer<? super V> action) {
        long l = lock.readLock();
        try {
            for(int i = 0; i < size.get(); i++)
                action.accept(elements[i].element);
        } finally {
            lock.unlockRead(l);
        }
    }

    public void forEach(@Nonnull BiConsumer<? super V, ? super Long> action) {
        long l = lock.readLock();
        try {
            for(int i = 0; i < size.get(); i++)
                action.accept(elements[i].element, elements[i].period);
        } finally {
            lock.unlockRead(l);
        }
    }

    @Override
    @Nonnull
    public ConcurrentLiveArrayList<V> clone() {
        long l = lock.readLock();
        try {
            ConcurrentLiveArrayList<V> liveArrayList = new ConcurrentLiveArrayList<>(defaultPeriod, size.get());
            liveArrayList.elements = new Element[elements.length];
            for(int i = 0; i < size.get(); i++)
                liveArrayList.elements[i] = elements[i].clone();
            liveArrayList.size.set(size.get());
            liveArrayList.started.set(started.get());
            liveArrayList.defaultPeriod = defaultPeriod;
            liveArrayList.delay = delay;
            liveArrayList.tickingPool = tickingPool;
            return liveArrayList;
        } finally {
            lock.unlockRead(l);
        }
    }

    @Override
    @Nullable
    public V get(int index) {
        long l = lock.readLock();
        try {
            checkRange(index);
            return elements[index].element;
        } finally {
            lock.unlockRead(l);
        }
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof ConcurrentLiveArrayList)) return false;

        ConcurrentLiveArrayList<?> that = (ConcurrentLiveArrayList<?>) o;
        long l = lock.readLock();
        try {
            return getDefaultPeriod() == that.getDefaultPeriod() && delay == that.delay && size.get() == that.size.get() && Arrays.equals(elements, that.elements) && started.get() == that.started.get();
        } finally {
            lock.unlockRead(l);
        }
    }

    @Override
    public int hashCode() {
        long l = lock.readLock();
        try {
            int result = size != null ? size.hashCode() : 0;
            result = 31 * result + (int) (getDefaultPeriod() ^ (getDefaultPeriod() >>> 32));
            result = 31 * result + Arrays.hashCode(elements);
            result = 31 * result + (int) (delay ^ (delay >>> 32));
            result = 31 * result + (started != null ? started.hashCode() : 0);
            result = 31 * result + (getTickingPool() != null ? getTickingPool().hashCode() : 0);
            return result;
        } finally {
            lock.unlockRead(l);
        }
    }

    public void start() {
        if(started.compareAndSet(false, true))
            tickingPool.startTicking(this, delay);
    }

    public void stop() {
        if(started.compareAndSet(true, false))
            tickingPool.stopTicking(this);
    }

    public void setTickingPool(@Nonnull TickingPool tickingPool, boolean start) {
        stop();
        this.tickingPool = tickingPool;
        if(start)
            start();
    }

    @Override
    public boolean isRunning() {
        return started.get();
    }

    @Override
    public void tick() {
        if(!isRunning())
            return;
        long l = lock.writeLock();
        try {
            long time = System.currentTimeMillis();
            for(int i = 0; i < size.get(); i++)
                if(!elements[i].isAlive(time)) {
                    remove0(i);
                    i--;
                }
        } finally {
            lock.unlockWrite(l);
        }
    }

    @Override
    public boolean objectEquals(Object other) {
        return this == other;
    }

    protected void resize(int required) {
        int oldCapacity = elements.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if(newCapacity - required < 0)
            newCapacity = required;
        if(newCapacity - MAX_ARRAY_SIZE > 0) {
            if(required < 0)
                throw new OutOfMemoryError();
            newCapacity = MAX_ARRAY_SIZE;
        }
        elements = Arrays.copyOf(elements, newCapacity);
    }

    protected void checkRange(int i) {
        if(i < 0 || i >= size.get())
            throw new IndexOutOfBoundsException("Index: " + i + ", size: " + size.get());
    }

    @ToString
    @EqualsAndHashCode
    protected static final class Element<V> implements Cloneable {
        protected volatile V element;
        protected volatile long creationTime;
        protected volatile long period;

        public Element(V element, long period) {
            this.element = element;
            this.period = period;
            this.creationTime = System.currentTimeMillis();
        }

        public boolean isAlive(long current) {
            return current - creationTime < period;
        }

        public void reset() {
            creationTime = System.currentTimeMillis();
        }

        public long liveTime() {
            return System.currentTimeMillis() - creationTime;
        }

        @Override
        protected Element<V> clone() {
            Element<V> clone;
            try {
                clone = (Element<V>) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new BadImplementationError("Thrown CloneNotSupportedException!");
            }
            clone.element = element;
            clone.creationTime = creationTime;
            clone.period = period;
            return clone;
        }
    }

    @ToString
    @EqualsAndHashCode
    protected final class IteratorImpl implements Iterator<V> {
        protected final AtomicInteger index = new AtomicInteger(0);

        @Override
        public boolean hasNext() {
            return index.get() < size();
        }

        @Override
        public V next() {
            if(index.get() >= size())
                throw new NoSuchElementException();
            return get(index.getAndIncrement());
        }

        @Override
        public void remove() {
            ConcurrentLiveArrayList.this.remove(index.decrementAndGet());
        }
    }

    @ToString
    @EqualsAndHashCode
    protected final class ListIteratorImpl implements ListIterator<V> {
        protected final AtomicInteger index;

        public ListIteratorImpl() {
            this(0);
        }

        public ListIteratorImpl(int index) {
            this.index = new AtomicInteger(index);
        }

        @Override
        public boolean hasNext() {
            return index.get() < size();
        }

        @Override
        public V next() {
            if(index.get() < size())
                return get(index.getAndIncrement());
            throw new NoSuchElementException();
        }

        @Override
        public boolean hasPrevious() {
            return index.get() > 0;
        }

        @Override
        public V previous() {
            if(index.get() <= 0)
                throw new NoSuchElementException();
            return get(index.getAndDecrement() - 1);
        }

        @Override
        public int nextIndex() {
            int size = size();
            if(index.get() < size)
                return index.get() + 1;
            return size;
        }

        @Override
        public int previousIndex() {
            return index.get() - 1;
        }

        @Override
        public void remove() {
            ConcurrentLiveArrayList.this.remove(index.decrementAndGet());
        }

        @Override
        public void set(V v) {
            ConcurrentLiveArrayList.this.set(index.get(), v);
        }

        @Override
        public void add(V v) {
            ConcurrentLiveArrayList.this.add(index.get(), v);
        }
    }
}
