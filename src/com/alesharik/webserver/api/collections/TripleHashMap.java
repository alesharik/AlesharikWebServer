package com.alesharik.webserver.api.collections;

import com.alesharik.webserver.api.functions.TripleConsumer;
import com.alesharik.webserver.api.functions.TripleFunction;
import com.alesharik.webserver.api.misc.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * This is a HashMap with three variables: key, value and addition.All functions have same functionality with {@link java.util.HashMap}
 *
 * @see java.util.HashMap
 */
public class TripleHashMap<K, V, A> {
    private static final int DEFAULT_CAPACITY = 16;

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private Entry[] entries;

    public TripleHashMap() {
        entries = new Entry[DEFAULT_CAPACITY];
    }

    public TripleHashMap(int initialCapacity) {
        entries = new Entry[initialCapacity];
    }

    public TripleHashMap(Map m) {
        this.entries = new Entry[m.size()];
        this.putAll(m);
    }

    public int size() {
        return entries.length;
    }

    public boolean isEmpty() {
        return entries.length < 1;
    }

    public Object get(Object key) {
        int bucket = getBucket(hash(key));
        Object value = null;
        Entry entry = entries[bucket];
        do {
            if(Integer.compare(entry.getHash(), hash(key)) == 0 && entry.getKey().equals(key)) {
                value = entry.getValue();
                break;
            }
        } while((entry = entry.next) != null);
        return value;
    }

    public Object getAddition(Object key) {
        int bucket = getBucket(hash(key));
        Object addition = null;
        Entry entry = entries[bucket];
        do {
            if(Integer.compare(entry.getHash(), hash(key)) == 0 && entry.getKey().equals(key)) {
                addition = entry.getAddition();
                break;
            }
        } while((entry = entry.next) != null);
        return addition;
    }

    public boolean containsKey(Object key) {
        int bucket = getBucket(hash(key));

        boolean contains = false;
        Entry entry = entries[bucket];
        do {
            if(Integer.compare(entry.getHash(), hash(key)) == 0 && entry.getKey().equals(key)) {
                contains = true;
                break;
            }
        } while((entry = entry.next) != null);
        return contains;
    }

    public boolean containsValue(Object value) {
        boolean contains = false;
        for(Entry entry : entries) {
            Entry entry1 = entry;
            do {
                if(entry1.getValue().equals(value)) {
                    contains = true;
                    break;
                }
            } while((entry1 = entry1.next) != null);
            if(contains) {
                break;
            }
        }
        return contains;
    }

    public boolean containsAddition(Object addition) {
        boolean contains = false;
        for(Entry entry : entries) {
            Entry entry1 = entry;
            do {
                if(entry1.getAddition().equals(addition)) {
                    contains = true;
                    break;
                }
            } while((entry1 = entry1.next) != null);
            if(contains) {
                break;
            }
        }
        return contains;
    }

    @SuppressWarnings("unchecked")
    public Object put(Object key, Object value, Object addition) {
        int bucket = getBucket(hash(key));
        if(bucket > entries.length - 1) {
            resize(bucket - entries.length - 1);
        }

        Entry old = entries[bucket];
        if(old != null) {
            do {
                if(Integer.compare(old.getHash(), hash(key)) == 0 && old.getKey().equals(key)) {
                    break;
                }
            } while((old = old.next) != null);
        }

        Entry oldEntry = old;

        if(old != null) {
            old.setValue(value);
            old.setAddition(addition);
        } else {
            final Entry<K, V, A> entry = new Entry<>((K) key, (V) value, (A) addition, hash(key));
            Entry entry1 = entries[bucket];
            if(entry1 != null) {
                while(entry1.next != null) {
                    entry1 = entry1.next;
                }
                entry1.next = entry;
            } else {
                entries[bucket] = entry;
            }
        }

        return oldEntry;
    }

    public void putAll(Map m) {
        if(m.size() > entries.length) {
            resize(m.size() - entries.length);
        }
        m.forEach((o, o2) -> put(o, o2, null));
    }

    public void putAll(TripleHashMap map) {
        map.forEach(this::put);
    }

    public Object remove(Object key) {
        if(!containsKey(key)) {
            return null;
        }

        int bucket = getBucket(hash(key));

        Entry entry = entries[bucket];
        int iterations = 0;
        do {
            iterations++;
            if(Integer.compare(entry.getHash(), hash(key)) == 0 && entry.getKey().equals(key)) {
                break;
            }
        } while((entry = entry.next) != null);
        Entry prew;

        Entry temp = null;
        for(int i = 0; i < iterations; i++) {
            if(i == 0) {
                temp = entries[bucket];
            }
            temp = temp.next;
        }
        prew = temp;

        Entry next;

        Entry temp1 = null;
        for(int i = 0; i < iterations + 2; i++) {
            if(i == 0) {
                temp1 = entries[bucket];
            }
            if(temp1.next == null) {
                temp1 = null;
                break;
            }
            temp1 = temp1.next;
        }
        next = temp1;

        if(next != null && prew != null) {
            prew.next = next;
        } else if(prew == null && next != null) {
            entries[bucket] = next;
        } else if(prew == null && next == null) {
            entries[bucket] = null;
        }
        return entry.getValue();
    }

    public void clear() {
        entries = new Entry[DEFAULT_CAPACITY];
    }


    public Set keySet() {
        Set<K> set = new HashSet<>();
        forEach((o, o2, o3) -> set.add((K) o));
        return set;
    }


    public Collection values() {
        ArrayList<V> values = new ArrayList<>();
        forEach((o, o2, o3) -> values.add((V) o2));
        return values;
    }

    public Collection additions() {
        ArrayList<A> additions = new ArrayList<>();
        forEach((o, o2, o3) -> additions.add((A) o3));
        return additions;
    }


    public Set<Triple> entrySet() {
        Set<Triple> entries = new HashSet<>();
        forEach((o, o2, o3) -> entries.add(Triple.immutable(o, o2, o3)));
        return entries;
    }


    public Object getOrDefault(Object key, Object defaultValue) {
        Object value;
        return (value = get(key)) == null ? defaultValue : value;
    }

    /**
     * Put triple if map don't have key
     *
     * @return Old value or null
     */
    public Object putIfAbsent(Object key, Object value, Object addition) {
        if(containsKey(key)) {
            return get(key);
        } else {
            put(key, value, addition);
        }
        return null;
    }

    public boolean remove(Object key, Object value) {
        Object val = get(key);
        if(val != null && val.equals(value)) {
            remove(key);
            return true;
        }
        return false;
    }

    /**
     * Replace value in key only if oldValue is valid
     *
     * @return true if replace success
     */
    public boolean replace(Object key, Object oldValue, Object newValue) {
        Object value = get(key);
        if(value == null || !value.equals(oldValue)) {
            return false;
        } else {
            put(key, newValue, getAddition(key));
            return true;
        }
    }

    /**
     * Replace value in key only if oldValue is valid
     *
     * @param addition set addition of replaceable key
     * @return true if replace success
     */
    public boolean replace(Object key, Object oldValue, Object newValue, Object addition) {
        Object value = get(key);
        if(value == null || !value.equals(oldValue)) {
            return false;
        } else {
            put(key, newValue, addition);
            return true;
        }
    }

    /**
     * If map don't contains the key, then put new triple with value from mappingFunction adn addition form additionalFunction
     *
     * @return existing value or generated value
     */
    public Object computeIfAbsent(Object key, Function mappingFunction, Function additionalFunction) {
        if(!containsKey(key)) {
            Object value = mappingFunction.apply(key);
            put(key, value, additionalFunction.apply(key));
            return value;
        } else {
            return get(key);
        }
    }

    /**
     * Use functions for remapping key if it present
     *
     * @return existing value or null
     */
    public Object computeIfPresent(Object key, TripleFunction remappingFunction, TripleFunction additionalFunction) {
        if(containsKey(key)) {
            Object value = remappingFunction.apply(key, get(key), getAddition(key));
            put(key, value, additionalFunction.apply(key, get(key), getAddition(key)));
            return value;
        }
        return null;
    }


    /**
     * Use functions for remapping key. Functions may receive null!
     *
     * @return value
     */
    public Object compute(Object key, TripleFunction remappingFunction, TripleFunction additionalFunction) {
        Object value = remappingFunction.apply(key, get(key), getAddition(key));
        put(key, value, additionalFunction.apply(key, get(key), getAddition(key)));
        return value;
    }

    /**
     * If key not associated with value then this function put key, value and addition into map. If key is associated
     * then return result of compute() function
     */
    public Object merge(Object key, Object value, Object addition, TripleFunction remappingFunction, TripleFunction additionalFunction) {
        if(!containsKey(key)) {
            put(key, value, addition);
            return value;
        } else {
            return compute(key, remappingFunction, additionalFunction);
        }
    }

    public void forEach(TripleConsumer action) {
        for(Entry entry : entries) {
            Entry entry1 = entry;
            do {
                action.accept(entry1.getKey(), entry1.getValue(), entry1.getAddition());
            } while((entry1 = entry1.next) != null);
        }
    }

    public void replaceAll(TripleFunction remappingFunction, TripleFunction additionalFunction) {
        forEach((o, o2, o3) -> put(o, remappingFunction.apply(o, o2, o3), additionalFunction.apply(o, o2, o3)));
    }

    @Override
    public TripleHashMap clone() throws CloneNotSupportedException {
        TripleHashMap map = ((TripleHashMap) super.clone());
        map.entries = entries;
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        TripleHashMap<?, ?, ?> that = (TripleHashMap<?, ?, ?>) o;

        return Arrays.equals(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(entries);
    }

    @Override
    public String toString() {
        return "TripleHashMap{" +
                "entries=" + Arrays.toString(entries) +
                '}';
    }

    private int getBucket(int hash) {
        return (entries.length - 1) & hash;
    }

    private void resize(int minCapacity) {
        int newCapacity = powerOfTwoFor(entries.length + minCapacity);
        Entry[] old = entries;
        entries = new Entry[newCapacity];
        for(Entry entry : old) {
            put(entry.getKey(), entry.getValue(), entry.getAddition());
        }
    }

    /**
     * REFERENCE: JAVA SOURCE CODE
     */
    private static int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * REFERENCE: JAVA SOURCE CODE
     */
    private static int powerOfTwoFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= Integer.MAX_VALUE) ? MAXIMUM_CAPACITY : n + 1;
    }


    private static class Entry<K, V, A> extends Triple<K, V, A> {
        private K key;
        private V value;
        private A addition;
        private int hash;
        private Entry next;

        public Entry(K key, V value, A addition, int hash) {
            this.key = key;
            this.value = value;
            this.addition = addition;
            this.hash = hash;
        }

        public int getHash() {
            return hash;
        }

        public void setHash(int hash) {
            this.hash = hash;
        }

        public Entry getNext() {
            return next;
        }

        public void setNext(Entry next) {
            this.next = next;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public A getAddition() {
            return addition;
        }

        public void setAddition(A addition) {
            this.addition = addition;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "key=" + key +
                    ", value=" + value +
                    ", addition=" + addition +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;

            Entry<?, ?, ?> entry = (Entry<?, ?, ?>) o;

            if(key != null ? !key.equals(entry.key) : entry.key != null) return false;
            if(value != null ? !value.equals(entry.value) : entry.value != null) return false;
            return addition != null ? addition.equals(entry.addition) : entry.addition == null;

        }

        @Override
        public int hashCode() {
            int result = key != null ? key.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            result = 31 * result + (addition != null ? addition.hashCode() : 0);
            return result;
        }
    }
}
