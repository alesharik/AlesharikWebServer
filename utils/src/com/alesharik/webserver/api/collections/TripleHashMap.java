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
import com.alesharik.webserver.api.misc.Triple;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * This is a HashMap with three variables: key, value and addition.All functions have same functionality with {@link java.util.HashMap}
 *
 * @see java.util.HashMap
 */
@EqualsAndHashCode
@NotThreadSafe
public class TripleHashMap<K, V, A> implements Cloneable, Serializable {
    private static final long serialVersionUID = 3751846878669761472L;

    protected static final int DEFAULT_CAPACITY = 16;
    protected static final int MAXIMUM_CAPACITY = 1 << 30;

    protected Entry<K, V, A>[] entries;
    protected int size;
    protected int sizeLimit;

    /**
     * Create map with default(16) capacity
     */
    public TripleHashMap() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Create this map from given map values and size
     *
     * @param m the map
     */
    public TripleHashMap(@Nonnull Map<K, V> m) {
        this(m.size());
        this.putAll(m);
    }

    /**
     * Create map with given capacity
     * @param initialCapacity the map capacity
     */
    @SuppressWarnings("unchecked")
    public TripleHashMap(int initialCapacity) {
        this.entries = new Entry[initialCapacity];
        this.sizeLimit = powerOfTwoFor(initialCapacity);
    }

    /**
     * Return currently holding keys count
     */
    public int size() {
        return size;
    }

    /**
     * Return <code>true</code> if map contains no elements, overwise <code>false</code>
     */
    public boolean isEmpty() {
        return size < 1;
    }

    /**
     * Return value using key
     *
     * @param key the key
     * @return value or <code>null</code> if key wasn't found. If key is <code>null</code>, then return value will be <code>null</code>
     */
    @Contract("null -> null; !null -> _")
    @Nullable
    public V get(@Nullable K key) {
        if(key == null)
            return null;

        int hash = hash(key);
        int bucket = getBucket(hash);
        Entry<K, V, A> entry = entries[bucket];
        if(entry == null)
            return null;

        do {
            if(entry.getHash() == hash && key.equals(entry.getKey()))
                return entry.getValue();
        } while((entry = entry.next) != null);

        return null;
    }

    @Contract("null -> null; !null -> _")
    @Nullable
    protected Entry<K, V, A> getEntry(@Nullable K key) {
        if(key == null)
            return null;

        int hash = hash(key);
        int bucket = getBucket(hash);
        Entry<K, V, A> entry = entries[bucket];
        if(entry == null)
            return null;

        do {
            if(entry.getHash() == hash && key.equals(entry.getKey()))
                return entry;
        } while((entry = entry.next) != null);

        return null;
    }

    /**
     * Return additional value using key
     *
     * @param key the key
     * @return additional value or <code>null</code> if key wasn't found. If key is <code>null</code>, then return value will be <code>null</code>
     */
    @Contract("null -> null; !null -> _")
    @Nullable
    public A getAddition(@Nullable K key) {
        if(key == null)
            return null;

        int hash = hash(key);
        int bucket = getBucket(hash);
        Entry<K, V, A> entry = entries[bucket];

        if(entry == null)
            return null;

        do {
            if(entry.getHash() == hash && key.equals(entry.getKey()))
                return entry.getAddition();
        } while((entry = entry.next) != null);
        return null;
    }

    /**
     * Return <code>true</code> if map contains key, overwise <code>false</code>
     *
     * @param key the key
     * @return <code>true</code> if map contains key, overwise <code>false</code>. If key is <code>null</code>, <code>false</code> will be returned
     */
    @Contract("null -> false")
    public boolean containsKey(@Nullable K key) {
        return containsKey0(key);
    }

    protected boolean containsKey0(@Nullable K key) {
        int hash = hash(key);
        int bucket = getBucket(hash);

        Entry entry = entries[bucket];
        if(entry == null)
            return false;

        do {
            if(entry.getHash() == hash && key.equals(entry.getKey()))
                return true;
        } while((entry = entry.next) != null);
        return false;
    }

    /**
     * Return <code>true</code> if map contains value, overwise <code>false</code>
     *
     * @param value the value
     * @return <code>true</code> if map contains value, overwise <code>false</code>. If value is <code>null</code>, <code>false</code> will be returned
     */
    @Contract("null -> false")
    public boolean containsValue(@Nullable V value) {
        if(value == null)
            return false;

        for(Entry entry : entries) {
            if(entry == null) continue;

            Entry subEntry = entry;
            do {
                if(value.equals(subEntry.getValue()))
                    return true;
            } while((subEntry = subEntry.next) != null);
        }
        return false;
    }


    /**
     * Return <code>true</code> if map contains additional value, overwise <code>false</code>
     *
     * @param addition additional value
     * @return <code>true</code> if map contains additional value, overwise <code>false</code>. If addition is <code>null</code>, <code>false</code> will be returned
     */
    @Contract("null -> false")
    public boolean containsAddition(@Nullable A addition) {
        if(addition == null)
            return false;

        for(Entry entry : entries) {
            if(entry == null) continue;

            Entry subEntry = entry;
            do {
                if(addition.equals(subEntry.getAddition()))
                    return true;
            } while((subEntry = subEntry.next) != null);
        }
        return false;
    }

    /**
     * Put new triple in map
     *
     * @param key      triple's key. Must be immutable
     * @param value    triple's value
     * @param addition triple's additional value
     * @return old triple with same key if it exists, overwise <code>null</code>
     */
    @Nullable
    public Triple<K, V, A> put(@Nonnull K key, @Nonnull V value, @Nullable A addition) {
        return putActual(key, value, addition);
    }

    @Nullable
    protected Triple<K, V, A> putActual(@Nonnull K key, @Nonnull V value, @Nullable A addition) {
        int hash = hash(key);
        int bucket = getBucket(hash);

        Entry<K, V, A> old = entries[bucket];
        if(old != null) {
            do {
                if(old.getHash() == hash && key.equals(old.getKey()))
                    break;
            } while((old = old.next) != null);
        }

        if(old != null) {//Already have - replace values/keys
            Triple<K, V, A> ret = old.convert();
            old.setKey(key);
            old.setValue(value);
            old.setAddition(addition);
            return ret;
        } else {//Add new entry
            Entry<K, V, A> entry = new Entry<>(key, value, addition, hash);

            Entry bucketEntry = entries[bucket];

            if(bucketEntry == null) //Don't have list in bucket
                entries[bucket] = entry;
            else {//We have list in bucket

                //Find last element
                while(bucketEntry.next != null)
                    bucketEntry = bucketEntry.next;

                //Set new bucket as last entry
                bucketEntry.next = entry;
            }

            size++;
            if(size > sizeLimit)
                resize(sizeLimit << 1);

            return null;
        }
    }

    /**
     * Add all elements from map to current map. All new element's additional values will be <code>null</code>
     *
     * @param m the map
     */
    public void putAll(@Nonnull Map<K, V> m) {
        m.forEach((k, v) -> putActual(k, v, null));
    }

    /**
     * Add all elements from map to current map
     *
     * @param map the map
     */
    public void putAll(@Nonnull TripleHashMap<K, V, A> map) {
        map.forEach(this::putActual);
    }

    /**
     * Remove element by key from map
     *
     * @param key the key
     * @return removed value, or <code>null</code> if map doesn't contains key. If key is <code>null</code> then <code>null</code> will be returned
     */
    @Nullable
    @Contract("null -> null")
    public Triple<K, V, A> remove(@Nullable K key) {
        if(key == null)
            return null;

        return removeActual(key);
    }

    @Nullable
    protected Triple<K, V, A> removeActual(@Nonnull K key) {
        int hash = hash(key);
        int bucket = getBucket(hash);

        //Find current entry for key
        Entry<K, V, A> entry = entries[bucket];
        if(entry == null)
            return null;

        Entry<K, V, A> prew = null;
        Entry<K, V, A> next;
        boolean equals = false;//Key exists
        do {
            Entry<K, V, A> prewTemp = entry;
            entry = entry.next;
            if(entry == null) {
                next = null;
                break;
            } else
                next = entry.next;

            if(entry.hash == hash && key.equals(entry.getKey())) {
                equals = true;
                break;
            }
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

    /**
     * Reset map entries and size
     */
    @SuppressWarnings("unchecked")
    public void clear() {
        entries = new Entry[DEFAULT_CAPACITY];
        size = 0;
    }

    /**
     * Return new map's key set
     * @return new {@link HashSet} instance
     */
    @Nonnull
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        forEach((o, o2, o3) -> set.add(o));
        return set;
    }

    /**
     * Return list of map's values
     * @return new {@link ArrayList} instance
     */
    @Nonnull
    public List<V> values() {
        ArrayList<V> values = new ArrayList<>();
        forEach((o, o2, o3) -> values.add(o2));
        return values;
    }

    /**
     * Return list of map's additional values
     * @return new {@link ArrayList} instance
     */
    @Nonnull
    public List<A> additions() {
        ArrayList<A> additions = new ArrayList<>();
        forEach((o, o2, o3) -> additions.add(o3));
        return additions;
    }

    /**
     * Return new set of map's entries
     *
     * @return new {@link HashSet} instance
     */
    @Nonnull
    public Set<Triple<K, V, A>> entrySet() {
        Set<Triple<K, V, A>> entries = new HashSet<>();
        for(Entry<K, V, A> entry : this.entries) {
            if(entry == null)
                continue;

            Entry<K, V, A> subEntry = entry;
            do {
                entries.add(subEntry.mimic());
            } while((subEntry = subEntry.next) != null);
        }
        return entries;
    }

    /**
     * If map contains value, it will return key's value, overwise default value
     *
     * @param key          value's key
     * @param defaultValue default value
     * @return value or default value
     */
    @Contract("_, null -> _; _, !null -> !null")
    @Nullable
    public V getOrDefault(@Nonnull K key, @Nullable V defaultValue) {
        V value;
        return (value = get(key)) == null ? defaultValue : value;
    }

    /**
     * If map contains additional value and it isn't <code>null</code>, it will return key's additional value, overwise default value
     * @param key additional value's key
     * @param defaultValue default value
     * @return value or default value
     */
    @Contract("_, null -> _; _, !null -> !null")
    @Nullable
    public A getOrDefaultAddition(@Nonnull K key, @Nullable A defaultValue) {
        A value;
        return (value = getAddition(key)) == null ? defaultValue : value;
    }

    /**
     * Put values if map doesn't contains key, overwise return current triple
     *
     * @param key      the key
     * @param value    it's value
     * @param addition it's addition
     * @return existing entry or <code>null</code>
     */
    @Nullable
    public Triple<K, V, A> putIfAbsent(@Nonnull K key, @Nonnull V value, @Nullable A addition) {
        Entry<K, V, A> entry = getEntry(key);
        if(entry != null)
            return entry.mimic();
        else {
            put(key, value, addition);
            return null;
        }
    }

    /**
     * Remove entry by key and value. Entry will be removed if it is exists and it's value equals provided value
     * @param key the key
     * @param value the value
     * @return true if entry removed, overwise false
     */
    public boolean remove(@Nonnull K key, @Nonnull V value) {
        V val = get(key);
        if(val != null && value.equals(val)) {
            remove(key);
            return true;
        }
        return false;
    }

    /**
     * Replace value in entry. If entry no exists, <code>false</code> will be returned
     * @param key value's key
     * @param newValue new value
     * @param oldValue old value
     * @return <code>true</code> if value is replaced, overwise false
     */
    public boolean replace(@Nonnull K key, @Nonnull V oldValue, @Nonnull V newValue) {
        Entry<K, V, A> entry = getEntry(key);

        if(entry == null || !oldValue.equals(entry.value))
            return false;

        entry.setValue(newValue);
        return true;
    }

    /**
     * Replace value in entry. If entry no exists, <code>false</code> will be returned
     * @param key value's key
     * @param newValue new value
     * @param oldValue old value
     * @param addition additional value
     * @return <code>true</code> if value is replaced, overwise false
     */
    public boolean replace(K key, V oldValue, V newValue, A addition) {
        Entry<K, V, A> entry = getEntry(key);

        if(entry == null || !oldValue.equals(entry.value))
            return false;

        entry.setValue(newValue);
        entry.setAddition(addition);
        return true;
    }

    /**
     * If map doesn't contains a key, new entry will be created by functions, overwise current entry will be returned
     * @param key the key
     * @param mappingFunction value's function
     * @param additionalFunction additional value's function
     * @return new entry or existing entry
     */
    @Nonnull
    public Triple<K, V, A> computeIfAbsent(@Nonnull K key, @Nonnull Function<? super K, ? extends V> mappingFunction, @Nonnull Function<? super K, ? extends A> additionalFunction) {
        if(!containsKey(key)) {
            V value = mappingFunction.apply(key);
            A addition = additionalFunction.apply(key);
            Triple<K, V, A> entry = put(key, value, addition);
            if(entry != null)
                throw new ConcurrentModificationException();
            return Triple.immutable(key, value, addition);
        } else
            return getEntry(key).mimic();
    }

    /**
     * Remap key's entry
     * @param key the key
     * @param remappingFunction value's remapping function
     * @param additionalFunction additional value's remapping function
     * @return existing remapped entry or <code>null</code>
     */
    @Nullable
    public Triple<K, V, A> computeIfPresent(@Nonnull K key, @Nonnull TripleFunction<? super K, ? super V, ? super A, ? extends V> remappingFunction, @Nonnull TripleFunction<? super K, ? super V, ? super A, ? extends A> additionalFunction) {
        Entry<K, V, A> entry = getEntry(key);
        if(entry == null)
            return null;
        else {
            entry.setValue(remappingFunction.apply(entry.getKey(), entry.getValue(), entry.getAddition()));
            entry.setAddition(additionalFunction.apply(entry.getKey(), entry.getValue(), entry.getAddition()));
            return entry.mimic();
        }
    }

    /**
     * Remap existing entry or create new entry
     * @param key the key
     * @param remappingFunction value's remapping function
     * @param additionalFunction additional value's remapping function
     * @return remapped entry
     */
    @Nonnull
    public Triple<K, V, A> compute(@Nonnull K key, @Nonnull TripleFunction<? super K, ? super V, ? super A, ? extends V> remappingFunction, @Nonnull TripleFunction<? super K, ? super V, ? super A, ? extends A> additionalFunction) {
        Entry<K, V, A> entry = getEntry(key);
        if(entry == null) {
            V val = remappingFunction.apply(key, null, null);
            A addition = additionalFunction.apply(key, val, null);
            put(key, val, addition);
            return Triple.immutable(key, val, addition);
        } else {
            entry.setValue(remappingFunction.apply(key, entry.getValue(), entry.getAddition()));
            entry.setAddition(additionalFunction.apply(key, entry.getValue(), entry.getAddition()));
            return entry.mimic();
        }
    }

    /**
     * Same as {@link #compute(Object, TripleFunction, TripleFunction)} function, bu use specified value and addition to create new entry
     * @param key the key
     * @param value new value
     * @param addition new addition
     * @param remappingFunction value's remapping function
     * @param additionalFunction additional value's remapping function
     * @return the entry
     */
    @Nonnull
    public Triple<K, V, A> merge(@Nonnull K key, @Nonnull V value, @Nonnull A addition, @Nonnull TripleFunction<? super K, ? super V, ? super A, ? extends V> remappingFunction, @Nonnull TripleFunction<? super K, ? super V, ? super A, ? extends A> additionalFunction) {
        if(!containsKey(key)) {
            put(key, value, addition);
            return Triple.immutable(key, value, addition);
        } else
            return compute(key, remappingFunction, additionalFunction);
    }

    /**
     * Iterates over all elements in map
     * @param action elements consumer
     */
    public void forEach(@Nonnull TripleConsumer<K, V, A> action) {
        for(Entry<K, V, A> entry : entries) {
            if(entry == null)
                continue;

            Entry<K, V, A> subEntry = entry;
            do {
                action.accept(subEntry.getKey(), subEntry.getValue(), subEntry.getAddition());
            } while((subEntry = subEntry.next) != null);
        }
    }

    /**
     * Replace all entries by values, supplied from functions
     *
     * @param remappingFunction  value's remapping function
     * @param additionalFunction additional value's remapping function
     */
    public void replaceAll(@Nonnull TripleFunction<? super K, ? super V, ? super A, ? extends V> remappingFunction, @Nonnull TripleFunction<? super K, ? super V, ? super A, ? extends A> additionalFunction) {
        for(Entry<K, V, A> entry : entries) {
            if(entry == null) continue;

            Entry<K, V, A> subEntry = entry;
            do {
                subEntry.setValue(remappingFunction.apply(subEntry.getKey(), subEntry.getValue(), subEntry.getAddition()));
                subEntry.setAddition(additionalFunction.apply(subEntry.getKey(), subEntry.getValue(), subEntry.getAddition()));
            } while((subEntry = subEntry.next) != null);
        }
    }

    /**
     * Clone the map
     */
    @Override
    @SneakyThrows
    public TripleHashMap<K, V, A> clone() {
        TripleHashMap<K, V, A> map = ((TripleHashMap<K, V, A>) super.clone());
        map.entries = new Entry[entries.length];
        for(int i = 0; i < entries.length; i++)
            map.entries[i] = entries[i].clone();
        map.size = size;
        map.sizeLimit = sizeLimit;
        return map;
    }

    @Override
    public String toString() {
        return "TripleHashMap{" +
                "entries=" + entrySet().toString() +
                ", size=" + size +
                '}';
    }

    @SuppressWarnings("unchecked")
    private void resize(int minCapacity) {
        int newCapacity = powerOfTwoFor(minCapacity);
        sizeLimit = newCapacity << 1;
        size = 0;
        Entry<K, V, A>[] old = entries;
        entries = new Entry[newCapacity];

        for(Entry<K, V, A> entry : old) {
            if(entry != null) {
                Entry<K, V, A> entry1 = entry;
                do {
                    putActual(entry1.getKey(), entry1.getValue(), entry1.getAddition());
                } while((entry1 = entry1.next) != null);
            }
        }
    }

    protected int getBucket(int hash) {
        return (entries.length - 1) & hash;
    }

    protected static int hash(@Nullable Object key) {
        if(key == null)
            return 0;

        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    /**
     * REFERENCE: JAVA SOURCE CODE
     */
    protected static int powerOfTwoFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= Integer.MAX_VALUE) ? MAXIMUM_CAPACITY : n + 1;
    }

    @Getter
    @ThreadSafe
    @EqualsAndHashCode(callSuper = false)
    @ToString
    protected static class Entry<K, V, A> extends Triple<K, V, A> implements Serializable {
        private static final long serialVersionUID = 4637937051986902968L;

        private volatile K key;
        private volatile V value;
        private volatile A addition;

        private volatile int hash;
        private volatile Entry<K, V, A> next;

        private transient volatile Triple.MutableTriple<K, V, A> mimic = null;

        public Entry(K key, V value, A addition, int hash) {
            this.key = key;
            this.value = value;
            this.addition = addition;
            this.hash = hash;
        }

        @Override
        public K getA() {
            return key;
        }

        @Override
        public V getB() {
            return value;
        }

        @Override
        public A getC() {
            return addition;
        }

        public void setKey(K key) {
            this.key = key;
            if(mimic != null)
                mimic.setA(key);
        }

        public void setValue(V value) {
            this.value = value;
            if(mimic != null)
                mimic.setB(value);
        }

        public void setAddition(A addition) {
            this.addition = addition;
            if(mimic != null)
                mimic.setC(addition);
        }

        @Override
        public Entry<K, V, A> clone() {
            Entry<K, V, A> clone = (Entry<K, V, A>) super.clone();
            clone.key = key;
            clone.value = value;
            clone.addition = addition;
            clone.hash = hash;
            if(next != null)
                clone.next = next.clone();
            else
                clone.next = null;
            return clone;
        }

        public Triple<K, V, A> convert() {
            return Triple.immutable(key, value, addition);
        }

        public Triple<K, V, A> mimic() {
            if(mimic == null)
                mimic = Triple.mutable(key, value, addition);
            return mimic;
        }
    }
}