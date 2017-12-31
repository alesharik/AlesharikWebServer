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

import com.alesharik.webserver.api.functions.TripleConsumer;
import com.alesharik.webserver.api.functions.TripleFunction;
import com.alesharik.webserver.exception.error.BadImplementationError;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.alesharik.webserver.api.collections.CollectionsMathUtils.*;

/**
 * This map updates only on method call.
 * If method has no lifeTime, it set DEFAULT_LIFE_TIME as lifeTime
 */
@EqualsAndHashCode
@ToString
@NotThreadSafe
public class CachedHashMap<K, V> implements Cloneable, Map<K, V>, Serializable, Stoppable {
    protected static final long DEFAULT_LIFE_TIME = TimeUnit.MINUTES.toMillis(1);
    protected static final int DEFAULT_CAPACITY = 16;

    private Entry<K, V>[] entries;
    private int size;
    private int sizeLimit;
    private boolean isRunning;

    public CachedHashMap() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Initialize new map instance
     *
     * @param count starting map bucket lists capacity
     * @throws IllegalArgumentException if count <= 0
     */
    public CachedHashMap(@Nonnegative int count) {
        if(count <= 0)
            throw new IllegalArgumentException("Count must be greater than 0!");
        count = powerOfTwoFor(count);
        this.sizeLimit = count;
        //noinspection unchecked
        this.entries = new Entry[count];
    }

    @Override
    public int size() {
        update();
        return size;
    }

    @Override
    public boolean isEmpty() {
        update();
        return size == 0;
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        if(key == null)
            return false;

        int hash = hash(key);
        int bucket = getBucket(hash, entries.length);
        Entry<K, V> entry = entries[bucket];
        if(entry == null)
            return false;

        long time = System.currentTimeMillis();
        do {
            while(entry.isExpired(time)) {
                Entry<K, V> n = removeEntry(bucket, entry);
                if(n != null)
                    entry = n;
                else
                    break;
            }
            if(entry == null)
                break;

            if(entry.hash == hash && key.equals(entry.getKey()))
                return true;
        } while((entry = entry.next) != null);
        return false;
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        long time = System.currentTimeMillis();
        for(Entry<K, V> entry : entries) {
            if(entry == null)
                continue;
            Entry<K, V> e = entry;
            do {
                while(entry.isExpired(time)) {
                    Entry<K, V> n = removeEntry(getBucket(e.hash, entries.length), entry);
                    if(n != null)
                        entry = n;
                    else
                        break;
                }
                if(entry == null)
                    break;

                if((e.value == null && value == null) || e.value.equals(value))
                    return true;
            } while((e = e.next) != null);
        }
        return false;
    }

    @Nullable
    @Override
    public V get(@Nullable Object key) {
        Entry<K, V> entry = getEntry(key);
        if(entry == null)
            return null;
        return entry.value;
    }

    public long getLiveTime(K key) {
        Entry<K, V> entry = getEntry(key);
        if(entry == null)
            return -1;
        return entry.getLiveTime();
    }

    public long getPeriod(K key) {
        Entry<K, V> entry = getEntry(key);
        if(entry == null)
            return -1;
        return entry.period;
    }

    public boolean resetTime(K key) {
        Entry<K, V> entry = getEntry(key);
        if(entry == null)
            return false;
        entry.startTime = System.currentTimeMillis();
        return true;
    }

    /**
     * Resets start time
     */
    public boolean setPeriod(K key, long period) {
        Entry<K, V> entry = getEntry(key);
        if(entry == null)
            return false;
        entry.period = period;
        entry.startTime = System.currentTimeMillis();
        return true;
    }

    @Contract("null -> null; !null -> _")
    @Nullable
    protected Entry<K, V> getEntry(@Nullable Object key) {
        if(key == null)
            return null;
        int hash = hash(key);
        int bucket = getBucket(hash, entries.length);
        Entry<K, V> entry = entries[bucket];
        if(entry == null)
            return null;

        long time = System.currentTimeMillis();
        do {
            while(entry.isExpired(time)) {
                Entry<K, V> n = removeEntry(bucket, entry);
                if(n != null)
                    entry = n;
                else
                    break;
            }
            if(entry == null)
                break;

            if(entry.hash == hash && key.equals(entry.getKey()))
                return entry;
        } while((entry = entry.next) != null);

        return null;
    }

    @Override
    public V put(K key, V value) {
        return put(key, value, DEFAULT_LIFE_TIME);
    }

    public V put(K key, V value, long period) {
        Map.Entry<K, V> kvEntry = putActual(new Entry<>(key, value, period));
        if(kvEntry == null)
            return null;
        return kvEntry.getValue();
    }

    @Override
    public V remove(Object key) {
        Map.Entry<K, V> kvEntry = removeActual(key);
        if(kvEntry == null)
            return null;
        return kvEntry.getValue();
    }

    protected boolean removeValue(Object value) {
        long time = System.currentTimeMillis();

        //Find current entry for key
        int b = 0;
        for(Entry<K, V> entry : entries) {
            int bucket = b;
            b++;
            if(entry == null)
                continue;

            Entry<K, V> prew = null;
            Entry<K, V> next = null;
            boolean equals = false;//Key exists
            do {
                Entry<K, V> prewTemp = entry;

                while(entry.isExpired(time)) {
                    Entry<K, V> n = removeEntry(bucket, entry);
                    if(n != null)
                        entry = n;
                    else
                        break;
                }
                if(entry == null)
                    break;

                if(value.equals(entry.value)) {
                    equals = true;
                    break;
                }

                entry = entry.next;
                if(entry == null) {
                    next = null;
                    break;
                } else
                    next = entry.next;
                prew = prewTemp;
            } while(entry != null);

            if(!equals)
                continue;

            if(prew != null)
                prew.next = next;
            else
                entries[bucket] = next;

            size--;
            return true;
        }
        return false;
    }

    @Nullable
    protected Map.Entry<K, V> removeActual(@Nonnull Object key) {
        long time = System.currentTimeMillis();
        int hash = hash(key);
        int bucket = getBucket(hash, entries.length);

        //Find current entry for key
        Entry<K, V> entry = entries[bucket];
        if(entry == null)
            return null;

        Entry<K, V> prew = null;
        Entry<K, V> next = null;
        boolean equals = false;//Key exists
        do {
            Entry<K, V> prewTemp = entry;

            while(entry.isExpired(time)) {
                Entry<K, V> n = removeEntry(bucket, entry);
                if(n != null)
                    entry = n;
                else
                    break;
            }
            if(entry == null)
                break;

            if(entry.hash == hash && key.equals(entry.getKey())) {
                equals = true;
                break;
            }

            entry = entry.next;
            if(entry == null) {
                next = null;
                entry = prewTemp;
                break;
            } else
                next = entry.next;
            prew = prewTemp;
        } while(entry != null);

        if(!equals)
            return null;

        if(prew != null)
            prew.next = next;
        else
            entries[bucket] = next;

        size--;
        return entry.convert();
    }

    protected Entry<K, V> removeEntry(int bucket, Entry<K, V> e) {
        Entry<K, V> entry = entries[bucket];
        if(entry == null)
            return null;

        Entry<K, V> prew = null;
        Entry<K, V> next = null;
        boolean equals = false;//Key exists
        do {
            Entry<K, V> prewTemp = entry;

            if(entry.hash == e.hash && e.key.equals(entry.getKey())) {
                equals = true;
                break;
            }

            entry = entry.next;
            if(entry == null) {
                next = null;
                break;
            } else
                next = entry.next;
            prew = prewTemp;
        } while(entry != null);

        if(!equals)
            return null;

        if(prew != null)
            prew.next = next;
        else
            entries[bucket] = next;
        size--;
        return next;
    }

    @Override
    public void putAll(@Nonnull Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    public void putAll(@Nonnull Map<? extends K, ? extends V> m, long period) {
        m.forEach((o, o2) -> put(o, o2, period));
    }

    public <A extends K, B extends V> void putAll(@Nonnull Map<A, B> m, BiFunction<A, B, Long> period) {
        m.forEach((a, b) -> put(a, b, period.apply(a, b)));
    }

    public <A extends K, B extends V> void putAllCached(@Nonnull CachedHashMap<A, B> m) {
        long time = System.currentTimeMillis();
        for(int i = 0; i < m.entries.length; i++) {
            Entry<A, B> entry = (Entry<A, B>) m.entries[i];
            if(entry == null)
                continue;

            Entry<A, B> e = entry;
            do {
                if(e.isExpired(time))
                    continue;
                putActual((Entry<K, V>) e.cloneElement());
            } while((e = e.next) != null);
        }
    }

    @Override
    public void clear() {
        //noinspection unchecked
        entries = new Entry[sizeLimit];
        size = 0;
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return new KeySetImpl();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return new ValuesCollectionImpl();
    }

    @NotNull
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new EntrySetImpl();
    }

    @Override
    public void start() {
        isRunning = true;
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * REMOVE {@link Entry#next}
     */
    protected Map.Entry<K, V> putActual(Entry<K, V> entry) {
        entry.next = null;

        long time = System.currentTimeMillis();
        int bucket = getBucket(entry.hash, entries.length);
        Entry<K, V> old = entries[bucket];
        if(old != null)
            do {
                while(old.isExpired(time)) {
                    Entry<K, V> n = removeEntry(bucket, old);
                    if(n != null)
                        old = n;
                    else
                        break;
                }
                if(old == null)
                    break;

                if(old.hash == entry.hash && old.key.equals(entry.key))
                    break;
            } while((old = old.next) != null);

        if(old != null) {
            Map.Entry<K, V> ret = old.convert();
            old.key = entry.key;
            old.value = entry.value;
            old.period = entry.period;
            old.startTime = entry.startTime;
            return ret;
        } else {
            Entry<K, V> head = entries[bucket];
            if(head == null)
                entries[bucket] = entry;
            else {

                while(head.next != null)
                    head = head.next;

                head.next = entry;
            }

            size++;
            if(size > sizeLimit)
                resize(sizeLimit << 1);
            return null;
        }
    }

    /**
     * REMOVE {@link Entry#next}. DO NOT CHECK SIZE
     */
    protected void addFast(Entry<K, V> entry) {
        entry.next = null;

        int bucket = getBucket(entry.hash, entries.length);
        Entry<K, V> head = entries[bucket];
        if(head == null)
            entries[bucket] = entry;
        else {

            while(head.next != null)
                head = head.next;

            head.next = entry;
        }
        size++;
    }

    @SuppressWarnings("unchecked")
    protected void resize(int minCapacity) {
        long time = System.currentTimeMillis();
        int newCapacity = powerOfTwoFor(minCapacity);
        sizeLimit = newCapacity << 1;
        size = 0;
        Entry<K, V>[] old = entries;
        entries = new Entry[newCapacity];

        for(Entry<K, V> entry : old) {
            if(entry == null)
                continue;

            Entry<K, V> e = entry;
            do {
                Entry<K, V> l = e;
                e = l.next;
                if(!l.isExpired(time))
                    addFast(l);
            } while(e != null);
        }
    }

    public void update() {
        long time = System.currentTimeMillis();
        for(Entry<K, V> entry : entries) {
            if(entry == null)
                continue;
            Entry<K, V> e = entry;
            do {
                while(entry.isExpired(time)) {
                    Entry<K, V> n = removeEntry(getBucket(e.hash, entries.length), entry);
                    if(n != null)
                        entry = n;
                    else
                        break;
                }
                if(entry == null)
                    break;
            } while((e = e.next) != null);
        }
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        long time = System.currentTimeMillis();
        for(Entry<K, V> entry : entries) {
            if(entry == null)
                continue;
            Entry<K, V> e = entry;
            do {
                while(entry.isExpired(time)) {
                    Entry<K, V> n = removeEntry(getBucket(e.hash, entries.length), entry);
                    if(n != null)
                        entry = n;
                    else
                        break;
                }
                if(entry == null)
                    break;
                action.accept(e.key, e.value);
            } while((e = e.next) != null);
        }
    }

    public void forEach(TripleConsumer<? super K, ? super V, Long> action) {
        long time = System.currentTimeMillis();
        for(Entry<K, V> entry : entries) {
            if(entry == null)
                continue;
            Entry<K, V> e = entry;
            do {
                while(entry.isExpired(time)) {
                    Entry<K, V> n = removeEntry(getBucket(e.hash, entries.length), entry);
                    if(n != null)
                        entry = n;
                    else
                        break;
                }
                if(entry == null)
                    break;
                action.accept(e.key, e.value, e.period);
            } while((e = e.next) != null);
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        long time = System.currentTimeMillis();
        for(Entry<K, V> entry : entries) {
            if(entry == null)
                continue;
            Entry<K, V> e = entry;
            do {
                while(entry.isExpired(time)) {
                    Entry<K, V> n = removeEntry(getBucket(e.hash, entries.length), entry);
                    if(n != null)
                        entry = n;
                    else
                        break;
                }
                if(entry == null)
                    break;
                e.value = function.apply(e.key, e.value);
                e.reset();
            } while((e = e.next) != null);
        }
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function, TripleFunction<? super K, ? super V, Long, Long> timeRemap) {
        long time = System.currentTimeMillis();
        for(Entry<K, V> entry : entries) {
            if(entry == null)
                continue;
            Entry<K, V> e = entry;
            do {
                while(entry.isExpired(time)) {
                    Entry<K, V> n = removeEntry(getBucket(e.hash, entries.length), entry);
                    if(n != null)
                        entry = n;
                    else
                        break;
                }
                if(entry == null)
                    break;
                e.value = function.apply(e.key, e.value);
                e.period = timeRemap.apply(e.key, e.value, e.period);
                e.reset();
            } while((e = e.next) != null);
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        if(!containsKey(key))
            return put(key, value);
        return get(key);
    }

    public V putIfAbsent(K key, V value, long period) {
        if(!containsKey(key))
            return put(key, value, period);
        return get(key);
    }

    @Override
    public boolean remove(Object key, Object value) {
        Entry<K, V> entry = getEntry(key);
        if(entry == null)
            return false;
        if(entry.value.equals(value)) {
            removeEntry(getBucket(entry.hash, entries.length), entry);
            return true;
        }
        return false;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Entry<K, V> entry = getEntry(key);
        if(entry == null || !entry.value.equals(oldValue))
            return false;
        entry.value = newValue;
        entry.reset();
        return true;
    }

    @Override
    public V replace(K key, V value) {
        Entry<K, V> entry = getEntry(key);
        if(entry == null)
            return null;
        V val = entry.value;
        entry.value = value;
        entry.reset();
        return val;
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Entry<K, V> entry = getEntry(key);
        if(entry != null)
            return entry.value;
        else {
            V val = mappingFunction.apply(key);
            put(key, val);
            return val;
        }
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, BiFunction<? super K, ? super V, Long> timeMapping) {
        Entry<K, V> entry = getEntry(key);
        if(entry != null)
            return entry.value;
        else {
            V val = mappingFunction.apply(key);
            put(key, val, timeMapping.apply(key, val));
            return val;
        }
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Entry<K, V> entry = getEntry(key);
        if(entry != null) {
            entry.value = remappingFunction.apply(entry.key, entry.value);
            entry.reset();
            return entry.value;
        }
        return null;
    }

    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, TripleFunction<? super K, ? super V, Long, Long> timeRemap) {
        Entry<K, V> entry = getEntry(key);
        if(entry != null) {
            entry.value = remappingFunction.apply(entry.key, entry.value);
            entry.period = timeRemap.apply(entry.key, entry.value, entry.period);
            entry.reset();
            return entry.value;
        }
        return null;
    }


    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Entry<K, V> entry = getEntry(key);
        if(entry != null) {
            entry.value = remappingFunction.apply(entry.key, entry.value);
            entry.reset();
            return entry.value;
        } else {
            V val = remappingFunction.apply(key, null);
            put(key, val);
            return val;
        }
    }

    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, TripleFunction<? super K, ? super V, Long, Long> timeRemap) {
        Entry<K, V> entry = getEntry(key);
        if(entry != null) {
            entry.value = remappingFunction.apply(entry.key, entry.value);
            entry.period = timeRemap.apply(entry.key, entry.value, entry.period);
            entry.reset();
            return entry.value;
        } else {
            V val = remappingFunction.apply(key, null);
            put(key, val, timeRemap.apply(key, null, DEFAULT_LIFE_TIME));
            return val;
        }
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Entry<K, V> entry = getEntry(key);
        if(entry == null) {
            put(key, value);
            return value;
        } else {
            entry.value = remappingFunction.apply(entry.value, value);
            entry.reset();
            return entry.value;
        }
    }

    public V merge(K key, V value, long period, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Entry<K, V> entry = getEntry(key);
        if(entry == null) {
            put(key, value, period);
            return value;
        } else {
            entry.value = remappingFunction.apply(entry.value, value);
            entry.period = period;
            entry.reset();
            return entry.value;
        }
    }

    @Override
    public CachedHashMap<K, V> clone() {
        try {
            CachedHashMap<K, V> clone = (CachedHashMap<K, V>) super.clone();
            clone.entries = new Entry[entries.length];
            for(int i = 0; i < entries.length; i++)
                clone.entries[i] = entries[i] == null ? null : entries[i].clone();
            clone.size = size;
            clone.sizeLimit = sizeLimit;
            clone.isRunning = isRunning;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new BadImplementationError("clone throw CloneNotSupportedException!");
        }
    }

    @NotThreadSafe
    @AllArgsConstructor
    @Getter
    @ToString
    private static final class UnBoundPair<KeyType, ValueType> implements Map.Entry<KeyType, ValueType> {
        private final KeyType key;
        private volatile ValueType value;

        @Override
        public ValueType setValue(ValueType value) {
            ValueType val = this.value;
            this.value = value;
            return val;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(!(o instanceof Map.Entry)) return false;

            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;

            return (getKey() != null ? getKey().equals(entry.getKey()) : entry.getKey() == null) && (getValue() != null ? getValue().equals(entry.getValue()) : entry.getValue() == null);
        }

        @Override
        public int hashCode() {
            int result = getKey() != null ? getKey().hashCode() : 0;
            result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
            return result;
        }
    }

    @NotThreadSafe
    @ToString
    protected class Entry<Key, Value> implements Map.Entry<Key, Value>, Serializable, Cloneable {
        private static final long serialVersionUID = -7767311238779888596L;

        @Getter
        protected volatile Key key;
        @Getter
        protected volatile Value value;
        protected volatile int hash;

        protected volatile Entry<Key, Value> next;

        protected volatile long startTime;
        protected volatile long period;

        public Entry(Key key, Value value, long period) {
            this.key = key;
            this.value = value;
            this.period = period;
            this.hash = hash(key);
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public Value setValue(Value value) {
            Value old = this.value;
            this.value = value;
            return old;
        }

        @Override
        public Entry<Key, Value> clone() {
            Entry<Key, Value> clone;
            try {
                clone = (Entry<Key, Value>) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new BadImplementationError("Clone throw an CloneNotSupportedException exception!");
            }
            clone.key = key;
            clone.value = value;
            clone.hash = hash;
            clone.next = next == null ? null : next.clone();
            return clone;
        }

        public Entry<Key, Value> cloneElement() {
            Entry<Key, Value> clone;
            try {
                clone = (Entry<Key, Value>) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new BadImplementationError("Clone throw an CloneNotSupportedException exception!");
            }
            clone.key = key;
            clone.value = value;
            clone.hash = hash;
            return clone;
        }

        public Map.Entry<Key, Value> convert() {
            return new UnBoundPair<>(key, value);
        }

        public boolean isExpired(long current) {
            return isRunning && (current - startTime) >= period;
        }

        public long getLiveTime() {
            return getLiveTime(System.currentTimeMillis());
        }

        public long getLiveTime(long current) {
            return current - startTime;
        }

        public void reset() {
            startTime = System.currentTimeMillis();
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(!(o instanceof Map.Entry)) return false;

            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;

            return (getKey() != null ? getKey().equals(entry.getKey()) : entry.getKey() == null) && (getValue() != null ? getValue().equals(entry.getValue()) : entry.getValue() == null);
        }

        @Override
        public int hashCode() {
            int result = getKey() != null ? getKey().hashCode() : 0;
            result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
            return result;
        }
    }

    private abstract class IteratorImpl<E> implements Iterator<E> {
        private Entry<K, V> next;
        private Entry<K, V> current;
        private int index;

        IteratorImpl() {
            Entry<K, V>[] t = entries;
            current = next = null;
            index = 0;
            if(t != null && size > 0) { // advance to first entry
                do {
                } while(index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Entry<K, V> nextNode() {
            Entry<K, V> e = next;
            if(e == null)
                throw new NoSuchElementException();
            Entry<K, V>[] t;
            if((next = (current = e).next) == null && (t = entries) != null) {
                do {
                } while(index < t.length && (next = t[index++]) == null);
            }
            return e;
        }

        public final void remove() {
            Entry<K, V> p = current;
            if(p == null)
                throw new IllegalStateException();
            current = null;
            removeEntry(getBucket(p.hash, entries.length), p);
        }
    }

    private final class KeyIterator extends IteratorImpl<K> {

        @Override
        public K next() {
            return nextNode().key;
        }
    }

    private final class ValueIterator extends IteratorImpl<V> {

        @Override
        public V next() {
            return nextNode().value;
        }
    }

    private final class EntryIterator extends IteratorImpl<Map.Entry<K, V>> {

        @Override
        public Map.Entry<K, V> next() {
            return nextNode().convert();
        }
    }

    private final class KeySetImpl implements Set<K> {

        @Override
        public int size() {
            return CachedHashMap.this.size;
        }

        @Override
        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public boolean contains(Object o) {
            return CachedHashMap.this.containsKey(o);
        }

        @NotNull
        @Override
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        @NotNull
        @Override
        public Object[] toArray() {
            Object[] arr = new Object[size()];
            Iterator<K> iterator = iterator();
            for(int i = 0; i < size; i++) {
                arr[i] = iterator.next();
            }
            return arr;
        }

        @NotNull
        @Override
        public <T> T[] toArray(@NotNull T[] a) {
            T[] arr = Arrays.copyOf(a, size());
            Iterator<K> iterator = iterator();
            for(int i = 0; i < size; i++) {
                arr[i] = (T) iterator.next();
            }
            return arr;
        }

        @Override
        public boolean add(K key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            return CachedHashMap.this.remove(o) != null;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            for(Object o : c) {
                if(!contains(o))
                    return false;
            }
            return true;
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends K> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            Iterator<K> iterator = iterator();
            boolean ok = false;
            while(iterator.hasNext()) {
                K next = iterator.next();
                if(!c.contains(next)) {
                    ok = true;
                    iterator.remove();
                }
            }
            return ok;
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            boolean ok = false;
            for(Object o : c) {
                if(remove(o))
                    ok = true;
            }
            return ok;
        }

        @Override
        public void clear() {
            CachedHashMap.this.clear();
        }
    }

    private final class ValuesCollectionImpl implements Collection<V> {

        @Override
        public int size() {
            return CachedHashMap.this.size;
        }

        @Override
        public boolean isEmpty() {
            return CachedHashMap.this.size == 0;
        }

        @Override
        public boolean contains(Object o) {
            return CachedHashMap.this.containsValue(o);
        }

        @NotNull
        @Override
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        @NotNull
        @Override
        public Object[] toArray() {
            Object[] arr = new Object[size()];
            Iterator<V> iterator = iterator();
            for(int i = 0; i < size; i++) {
                arr[i] = iterator.next();
            }
            return arr;
        }

        @NotNull
        @Override
        public <T> T[] toArray(@NotNull T[] a) {
            T[] arr = Arrays.copyOf(a, size());
            Iterator<V> iterator = iterator();
            for(int i = 0; i < size; i++) {
                arr[i] = (T) iterator.next();
            }
            return arr;
        }

        @Override
        public boolean add(V key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            return CachedHashMap.this.remove(o) != null;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            for(Object o : c) {
                if(!contains(o))
                    return false;
            }
            return true;
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends V> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            Iterator<V> iterator = iterator();
            boolean ok = false;
            while(iterator.hasNext()) {
                V next = iterator.next();
                if(!c.contains(next)) {
                    ok = true;
                    iterator.remove();
                }
            }
            return ok;
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            boolean ok = false;
            for(Object o : c) {
                if(removeValue(o))
                    ok = true;
            }
            return ok;
        }

        @Override
        public void clear() {
            CachedHashMap.this.clear();
        }
    }

    private final class EntrySetImpl implements Set<Map.Entry<K, V>> {

        @Override
        public int size() {
            return CachedHashMap.this.size;
        }

        @Override
        public boolean isEmpty() {
            return CachedHashMap.this.size == 0;
        }

        @Override
        public boolean contains(Object o) {
            if(!(o instanceof Map.Entry))
                throw new IllegalArgumentException("Collection elements must be Map.Entry!");
            return CachedHashMap.this.containsKey(((Map.Entry) o).getKey());
        }

        @NotNull
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @NotNull
        @Override
        public Object[] toArray() {
            Object[] arr = new Object[size()];
            Iterator<Map.Entry<K, V>> iterator = iterator();
            for(int i = 0; i < size; i++) {
                arr[i] = iterator.next();
            }
            return arr;
        }

        @NotNull
        @Override
        public <T> T[] toArray(@NotNull T[] a) {
            T[] arr = Arrays.copyOf(a, size());
            Iterator<Map.Entry<K, V>> iterator = iterator();
            for(int i = 0; i < size; i++) {
                arr[i] = (T) iterator.next();
            }
            return arr;
        }

        @Override
        public boolean add(Map.Entry<K, V> key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            if(!(o instanceof Map.Entry))
                throw new IllegalArgumentException("Collection elements must be Map.Entry!");
            return CachedHashMap.this.remove(((Map.Entry) o).getKey()) != null;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            for(Object o : c) {
                if(!(o instanceof Map.Entry))
                    throw new IllegalArgumentException("Collection elements must be Map.Entry!");

                if(!contains(o))
                    return false;
            }
            return true;
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends Map.Entry<K, V>> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            Iterator<Map.Entry<K, V>> iterator = iterator();
            boolean ok = false;
            while(iterator.hasNext()) {
                Map.Entry<K, V> next = iterator.next();
                if(!c.contains(next)) {
                    iterator.remove();
                    ok = true;
                }
            }
            return ok;
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            boolean ok = false;
            for(Object o : c) {
                if(remove(o))
                    ok = true;
            }
            return ok;
        }

        @Override
        public void clear() {
            CachedHashMap.this.clear();
        }
    }
}
