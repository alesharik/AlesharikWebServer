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

import com.alesharik.webserver.exception.error.BadImplementationError;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This list is used for storing values with specific live time. It doesn't like <code>null</code> element. Every value has
 * it's creation time and period. If elapsed time from creation time will be >= period, value will be deleted from list. All
 * time is represented in milliseconds
 * @param <V> element type
 */
@NotThreadSafe
@EqualsAndHashCode
@ToString
public class CachedArrayList<V> implements Cloneable, List<V>, RandomAccess, Serializable, Stoppable {
    protected static final long DEFAULT_LIFE_TIME_PERIOD = TimeUnit.MINUTES.toMillis(60);
    private static final long serialVersionUID = -1792443024245024351L;
    protected static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    protected boolean isRunning;

    protected TimedElement<V>[] elements;
    protected int size;

    /**
     * Create empty list with <code>16</code> initial capacity and current time as last update time
     */
    public CachedArrayList() {
        this(16);
    }

    /**
     * Create empty list with specified initial capacity and current time as last update time
     *
     * @param count initial capacity
     */
    public CachedArrayList(int count) {
        this.elements = new TimedElement[count];
        this.isRunning = true;
    }

    /**
     * Add new element to list with {@link #DEFAULT_LIFE_TIME_PERIOD} life time
     *
     * @param v new value
     * @return <code>true</code> if value was added, <code>false</code> overwise. If value == <code>null</code>, <code>false</code> will be returned
     */
    public boolean add(@Nullable V v) {
        return add(v, DEFAULT_LIFE_TIME_PERIOD);
    }

    /**
     * Add new element to list with specified life time
     *
     * @param v        new value
     * @param lifeTime life time
     * @return <code>true</code> if value was added, <code>false</code> overwise. If value == <code>null</code>, <code>false</code> will be returned
     * @throws IllegalArgumentException if lifeTime < <code>0</code>
     */
    public boolean add(@Nullable V v, long lifeTime) {
        if(v == null)
            return false;
        if(lifeTime < 0)
            throw new IllegalArgumentException("Time must be positive or zero!");
        int next = size;
        if(!checkCapacityAdd(next))
            return false;
        size++;
        elements[next] = new TimedElement<>(v, System.currentTimeMillis(), lifeTime);
        return true;
    }

    @Override
    public void add(int index, @Nonnull V element) {
        add(index, element, DEFAULT_LIFE_TIME_PERIOD);
    }

    public void add(int index, @Nonnull V element, long lifeTime) {
        checkCapacity(index);
        if(!grow(size + 1))
            throw new ArrayIndexOutOfBoundsException("Overflow");
        System.arraycopy(elements, index, elements, index + 1, size - index);
        TimedElement<V> element1 = new TimedElement<>(element, System.currentTimeMillis(), lifeTime);
        elements[index] = element1;
        size++;
    }

    @Nonnull
    @Override
    public V set(int index, @Nonnull V element) {
        return set(index, element, DEFAULT_LIFE_TIME_PERIOD);
    }

    @Nonnull
    public V set(int index, @Nonnull V element, long lifeTime) {
        checkCapacity(index);
        TimedElement<V> elem = getElement(index);
        V v = elem.t;
        elem.t = element;
        elem.period = lifeTime;
        elem.startTime = System.currentTimeMillis();
        return v;
    }

    public void setPeriod(int index, long time) {
        checkCapacity(index);
        TimedElement<V> element = getElement(index);
        element.startTime = System.currentTimeMillis();
        element.period = time;
    }

    @Nonnull
    protected CachedArrayList.TimedElement<V> getElement(int index) {
        TimedElement<V> element;
        boolean ok;
        long current = System.currentTimeMillis();
        do {
            if(index >= size)
                throw new ArrayIndexOutOfBoundsException(index);
            element = elements[index];
            if(isRunning && !element.check(current)) {
                fastRemove(index);
                ok = false;
                index++;
            } else
                ok = true;
        } while(!ok);
        return element;
    }

    public boolean setPeriod(@Nonnull V o, long per) {
        long time = System.currentTimeMillis();
        for(int i = 0; i < size; i++) {
            TimedElement<V> element = elements[i];
            if(!isRunning || element.check(time)) {
                if(element.t.equals(o)) {
                    element.startTime = System.currentTimeMillis();
                    element.period = per;
                    return true;
                }
            } else {
                fastRemove(i);
                i--;
            }
        }
        return false;
    }

    public long getLiveTime(@Nonnull V obj) {
        long time = System.currentTimeMillis();
        for(int i = 0; i < size; i++) {
            TimedElement<V> element = elements[i];
            if(!isRunning || element.check(time)) {
                if(element.t.equals(obj)) {
                    return element.getLiveTime();
                }
            } else {
                fastRemove(i);
                i--;
            }
        }
        return -1;
    }

    public long getLiveTime(int index) {
        checkCapacity(index);
        return getElement(index).getLiveTime();
    }

    public long getLastTime(@Nonnull V obj) {
        long time = System.currentTimeMillis();
        for(int i = 0; i < size; i++) {
            TimedElement<V> element = elements[i];
            if(!isRunning || element.check(time)) {
                if(element.t.equals(obj)) {
                    return element.period - element.getLiveTime();
                }
            } else {
                fastRemove(i);
                i--;
            }
        }
        return -1;
    }

    public long getLastTime(int index) {
        checkCapacity(index);
        TimedElement<V> element = getElement(index);
        return element.period - element.getLiveTime();
    }

    public void resetTime(int index) {
        getElement(index).startTime = System.currentTimeMillis();
    }

    public boolean resetTime(Object o) {
        long time = System.currentTimeMillis();
        for(int i = 0; i < size; i++) {
            TimedElement<V> element = elements[i];
            if(!isRunning || element.check(time)) {
                if(element.t.equals(o)) {
                    element.startTime = System.currentTimeMillis();
                    return true;
                }
            } else {
                fastRemove(i);
                i--;
            }
        }
        return false;
    }

    @Override
    public V remove(int index) {
        checkCapacity(index);
        TimedElement<V> element;
        boolean ok;
        long current = System.currentTimeMillis();
        do {
            if(index >= size)
                throw new ArrayIndexOutOfBoundsException(index);
            element = elements[index];
            if(isRunning && !element.check(current)) {
                ok = false;
                index++;
            } else
                ok = true;
            fastRemove(index);
        } while(!ok);

        return element.t;
    }

    @Override
    public int indexOf(Object o) {
        long time = System.currentTimeMillis();
        for(int i = 0; i < size; i++) {
            TimedElement<V> element = elements[i];
            if(!isRunning || element.check(time)) {
                if(element.t.equals(o)) {
                    return i;
                }
            } else {
                fastRemove(i);
                i--;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if(size == 0)
            return -1;

        long time = System.currentTimeMillis();
        for(int i = size - 1; i >= 0; i--) {
            TimedElement<V> element = elements[i];
            if(!isRunning || element.check(time)) {
                if(element.t.equals(o)) {
                    return i;
                }
            } else {
                fastRemove(i);
                i++;
            }
        }
        return -1;
    }

    @Override
    public void clear() {
        elements = new TimedElement[size];
        size = 0;
    }

    @Override
    public boolean addAll(int index, Collection<? extends V> c) {
        return addAll(index, c, DEFAULT_LIFE_TIME_PERIOD);
    }

    public boolean addAll(int index, @Nonnull Collection<? extends V> c, long lifeTime) {
        if(c.size() == 0 || !checkCapacityAdd(index) || !checkCapacityAdd(index + c.size()))
            return false;
        Object[] arr = c.toArray();
        for(int i = index; i < c.size() + index; i++) {
            TimedElement<V> element = elements[i];
            if(element == null)
                elements[i] = new TimedElement<>((V) arr[i - index], System.currentTimeMillis(), lifeTime);
            else {
                element.t = (V) arr[i - index];
                element.startTime = System.currentTimeMillis();
                element.period = lifeTime;
            }
        }
        int delta = index - size;
        size += delta + c.size();
        return true;
    }

    @Override
    @Nonnull
    public Iterator<V> iterator() {
        update();
        return new IteratorImpl<>();
    }

    @Override
    @Nonnull
    public ListIterator<V> listIterator() {
        update();
        //noinspection SimplifyStreamApiCallChains
        return stream().collect(Collectors.toList()).listIterator();
    }

    @Override
    @Nonnull
    public ListIterator<V> listIterator(int index) {
        update();
        //noinspection SimplifyStreamApiCallChains
        return stream().collect(Collectors.toList()).listIterator(index);
    }

    @Override
    @Nonnull
    public List<V> subList(int fromIndex, int toIndex) {
        update();
        checkCapacity(fromIndex);
        checkCapacity(toIndex - 1);

        List<V> ret = new ArrayList<>();
        for(int i = fromIndex; i < toIndex; i++)
            ret.add(elements[i].t);
        return ret;
    }

    @Override
    public boolean isEmpty() {
        update();
        return size == 0;
    }

    @Override
    public boolean contains(@Nonnull Object o) {
        long time = System.currentTimeMillis();
        for(int i = 0; i < size; i++) {
            TimedElement<V> element = elements[i];
            if(!isRunning || element.check(time)) {
                if(element.t.equals(o)) {
                    return true;
                }
            } else {
                fastRemove(i);
                i--;
            }
        }
        return false;
    }

    @Override
    @Nonnull
    public Object[] toArray() {
        update();
        Object[] a = new Object[size];
        for(int i = 0; i < size; i++)
            a[i] = elements[i].t;
        return a;
    }

    @Override
    @Contract("null -> null; !null -> !null")
    @Nullable
    public <C> C[] toArray(@Nullable C[] a) {
        if(a == null)
            return null;
        update();
        a = Arrays.copyOf(a, size);
        for(int i = 0; i < size; i++)
            a[i] = (C) elements[i].t;
        return a;
    }

    @Override
    public boolean remove(@Nonnull Object o) {
        long time = System.currentTimeMillis();
        for(int i = 0; i < size; i++) {
            TimedElement<V> element = elements[i];
            if(!isRunning || element.check(time)) {
                if(element.t.equals(o)) {
                    fastRemove(i);
                    return true;
                }
            } else {
                fastRemove(i);
                i--;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {
        update();
        for(Object o : c) {
            if(!contains(o))
                return false;
        }
        return true;
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
        boolean ok = false;
        for(Object o : c)
            if(remove(o))
                ok = true;
        return ok;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean retainAll(@Nonnull Collection<?> c) {
        return retainAll((Collection<? extends V>) c, DEFAULT_LIFE_TIME_PERIOD);
    }

    public boolean retainAll(@Nonnull Collection<? extends V> c, long lifeTime) {
        clear();
        return addAll(c, lifeTime);
    }

    /**
     * Reset elements times to default
     * {@inheritDoc}
     */
    @Override
    public void replaceAll(@Nonnull UnaryOperator<V> operator) {
        long time = System.currentTimeMillis();
        for(int i = 0; i < size; i++) {
            TimedElement<V> element = elements[i];
            if(!isRunning || element.check(time)) {
                element.t = operator.apply(element.t);
                element.period = DEFAULT_LIFE_TIME_PERIOD;
                element.startTime = time;
            } else {
                fastRemove(i);
                i--;
            }
        }
    }

    /**
     * Reset elements times
     * {@link #replaceAll(UnaryOperator)}
     */
    public void replaceAll(@Nonnull UnaryOperator<V> operator, @Nonnull UnaryOperator<Long> lifeTimeOperator) {
        long time = System.currentTimeMillis();
        for(int i = 0; i < size; i++) {
            TimedElement<V> element = elements[i];
            if(!isRunning || element.check(time)) {
                element.t = operator.apply(element.t);
                element.period = lifeTimeOperator.apply(element.period);
                element.startTime = time;
            } else {
                fastRemove(i);
                i--;
            }
        }
    }

    /**
     * {@inheritDoc}
     * @param c null - basic sort
     */
    @Override
    public void sort(@Nullable Comparator<? super V> c) {
        if(c == null) {
            Arrays.sort(elements, (o1, o2) -> {
                if(o1 == null && o2 == null) return 0;
                if(o1 == null) return +1;
                if(o2 == null) return -1;

                return o1.compareTo(o2);
            });
        } else
            Arrays.sort(elements, (o1, o2) -> {
                if(o1 == null && o2 == null) return 0;
                if(o1 == null) return +1;
                if(o2 == null) return -1;

                return c.compare(o1.t, o2.t);
            });
    }

    @Nonnull
    @Override
    public Spliterator<V> spliterator() {
        return stream().spliterator();
    }

    @Override
    public boolean removeIf(@Nonnull Predicate<? super V> filter) {
        long time = System.currentTimeMillis();
        boolean ok = false;
        for(int i = 0; i < size; i++) {
            TimedElement<V> element = elements[i];
            if(!isRunning || element.check(time)) {
                if(filter.test(element.t)) {
                    fastRemove(i);
                    i--;
                    ok = true;
                }
            } else {
                fastRemove(i);
                i--;
            }
        }
        return ok;
    }

    @Nonnull
    @Override
    public Stream<V> stream() {
        Stream.Builder<V> builder = Stream.builder();
        forEach(builder);
        return builder.build();
    }

    @Nonnull
    @Override
    public Stream<V> parallelStream() {
        Stream.Builder<V> builder = Stream.builder();
        forEach(builder);
        return builder.build().parallel();
    }

    @Override
    public void forEach(@Nonnull Consumer<? super V> action) {
        long time = System.currentTimeMillis();
        for(int i = 0; i < size; i++) {
            TimedElement<V> element = elements[i];
            if(!isRunning || element.check(time))
                action.accept(element.t);
            else {
                fastRemove(i);
                i--;
            }
        }
    }

    public void forEach(@Nonnull BiConsumer<? super V, ? super Long> action) {
        long time = System.currentTimeMillis();
        for(int i = 0; i < size; i++) {
            TimedElement<V> element = elements[i];
            if(!isRunning || element.check(time))
                action.accept(element.t, element.getLiveTime());
            else {
                fastRemove(i);
                i--;
            }
        }
    }

    @Nonnull
    @Override
    public CachedArrayList<V> clone() {
        update();
        CachedArrayList<V> list;
        try {
            list = ((CachedArrayList<V>) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new BadImplementationError("Clone throw an CloneNotSupportedException exception!");
        }
        list.elements = new TimedElement[list.elements.length];
        for(int i = 0; i < size; i++)
            list.elements[i] = elements[i].clone();
        list.size = size;
        list.isRunning = isRunning;
        return list;
    }

    @Override
    @Nonnull
    public V get(int index) {
        checkCapacity(index);

        return getElement(index).t;
    }

    @Override
    public int size() {
        update();
        return size;
    }

    public void update() {
        if(!isRunning)
            return;
        long time = System.currentTimeMillis();
        updateValues(time);
    }

    protected void updateValues(long time) {
        for(int i = 0; i < size; i++) {
            if(!elements[i].check(time)) {
                fastRemove(i);
                i--;
            }
        }
    }

    public void start() {
        isRunning = true;
    }

    public void stop() {
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    protected void fastRemove(int index) {
        int numMoved = size - index - 1;
        if(numMoved > 0)
            System.arraycopy(elements, index + 1, elements, index, numMoved);
        elements[--size] = null;
    }

    protected void checkCapacity(int index) {
        if(index < 0 || index >= size)
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
    }

    protected boolean checkCapacityAdd(int index) {
        if(index < 0)
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
        return index < elements.length || grow(index);
    }

    private boolean grow(int size) {
        if(elements.length == MAX_ARRAY_SIZE)
            return false;

        int capacity = elements.length + (elements.length >> 1);
        if(capacity < size)
            capacity = size;
        if(capacity > MAX_ARRAY_SIZE)
            capacity = MAX_ARRAY_SIZE;

        elements = Arrays.copyOf(elements, capacity);
        return true;
    }

    @ToString
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    protected static class TimedElement<T> implements Cloneable, Comparable<TimedElement<T>>, Serializable {
        private static final long serialVersionUID = -5056458851895169362L;
        protected volatile T t;
        protected volatile long startTime;
        protected volatile long period;

        /**
         * Check if element is alive
         *
         * @param current current time
         * @return <code>true</code> - alive, overwise <code>false</code>
         */
        public boolean check(long current) {
            return getLiveTime(current) < period;
        }

        public long getLiveTime() {
            return getLiveTime(System.currentTimeMillis());
        }

        public long getLiveTime(long current) {
            return current - startTime;
        }

        @Override
        protected TimedElement<T> clone() {
            TimedElement<T> clone;
            try {
                clone = (TimedElement<T>) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new BadImplementationError("Clone throw an CloneNotSupportedException exception!");
            }
            clone.t = t;
            clone.startTime = startTime;
            clone.period = period;
            return clone;
        }

        @Override
        public int compareTo(TimedElement<T> other) {
            if(t instanceof Comparable)
                return ((Comparable) t).compareTo(other.t);
            return CompareToBuilder.reflectionCompare(t, other.t);
        }
    }

    private final class IteratorImpl<A> implements Iterator<A> {
        private int index;

        @Override
        public boolean hasNext() {
            return index < size;
        }

        @Override
        public A next() {
            if(!hasNext())
                throw new NoSuchElementException();

            try {
                return (A) elements[index].t;
            } finally {
                index++;
            }
        }

        @Override
        public void remove() {
            fastRemove(index);
            index--;
        }
    }
}
