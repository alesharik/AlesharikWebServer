package com.alesharik.webserver.api.collections;

import com.alesharik.webserver.api.functions.TripleConsumer;
import com.alesharik.webserver.api.functions.TripleFunction;
import com.alesharik.webserver.api.misc.Triple;

import java.io.Serializable;
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
public class TripleHashMap<K, V, A> implements Cloneable, Serializable {
    private static final int DEFAULT_CAPACITY = 16;

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private Entry[] entries;
    private int size;

    private int sizeLimit = 32;

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
        return size;
    }

    public boolean isEmpty() {
        return entries.length < 1;
    }

    public Object get(Object key) {
        int bucket = getBucket(hash(key));
        Object value = null;
        Entry entry = entries[bucket];
        if(entry == null) {
            return null;
        }
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
            if(entry == null) {
                continue;
            }

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
            if(entry == null) {
                continue;
            }

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

        size++;
        if(size > sizeLimit) {
            resize(sizeLimit << 1);
        }

        return oldEntry;
    }

    @SuppressWarnings("unchecked")
    public void putAll(Map m) {
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
        if(entry != null) {
            size--;
            return entry.getValue();
        } else {
            return null;
        }
    }

    public void clear() {
        entries = new Entry[DEFAULT_CAPACITY];
    }


    @SuppressWarnings("unchecked")
    public Set keySet() {
        Set<K> set = new HashSet<>();
        forEach((o, o2, o3) -> set.add((K) o));
        return set;
    }


    @SuppressWarnings("unchecked")
    public Collection values() {
        ArrayList<V> values = new ArrayList<>();
        forEach((o, o2, o3) -> values.add((V) o2));
        return values;
    }

    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
    public void forEach(TripleConsumer action) {
        for(Entry entry : entries) {
            if(entry == null) {
                continue;
            }

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
        int newCapacity = powerOfTwoFor(minCapacity);
        sizeLimit = newCapacity << 1;
        size = 0;
        Entry[] old = entries;
        entries = new Entry[newCapacity];
        for(Entry entry : old) {
            if(entry != null) {
                Entry entry1 = entry;
                do {
                    put(entry1.getKey(), entry1.getValue(), entry1.getAddition());
                } while((entry1 = entry1.next) != null);
            }
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
        private int hash;
        private Entry next;

        public Entry(K key, V value, A addition, int hash) {
            this.a = key;
            this.b = value;
            this.c = addition;
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
            return a;
        }

        public void setKey(K key) {
            this.a = key;
        }

        public V getValue() {
            return b;
        }

        public void setValue(V value) {
            this.b = value;
        }

        public A getAddition() {
            return c;
        }

        public void setAddition(A addition) {
            this.c = addition;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "key=" + a +
                    ", value=" + b +
                    ", addition=" + c +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;

            Entry<?, ?, ?> entry = (Entry<?, ?, ?>) o;

            if(a != null ? !a.equals(entry.a) : entry.a != null) return false;
            if(b != null ? !b.equals(entry.b) : entry.b != null) return false;
            return c != null ? c.equals(entry.c) : entry.c == null;

        }

        @Override
        public int hashCode() {
            int result = a != null ? a.hashCode() : 0;
            result = 31 * result + (b != null ? b.hashCode() : 0);
            result = 31 * result + (c != null ? c.hashCode() : 0);
            return result;
        }
    }
}