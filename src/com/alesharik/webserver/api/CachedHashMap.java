package com.alesharik.webserver.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class CachedHashMap<K, V> implements Map<K, V> {
    private final CachedArrayList<Entry> entries;

    public CachedHashMap() {
        entries = new CachedArrayList<>();
    }

    public CachedHashMap(int capacity) {
        entries = new CachedArrayList<>(capacity);
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        for(Entry entry : entries) {
            if(entry.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        for(Entry entry : entries) {
            if(entry.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        for(Entry entry : entries) {
            if(entry.getKey().equals(key)) {
                return (V) entry.getValue();
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    public void put(K key, V value, long timeout) {
        entries.add(new Entry<>(key, value), timeout);
    }

    @Override
    public V remove(Object key) {
        for(Entry entry : entries) {
            if(entry.getKey().equals(key)) {
                entries.delete(entry);
                return (V) entry.getValue();
            }
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map<? extends K, ? extends V> m, long timeout) {
        m.forEach((o, o2) -> {
            put(o, o2, timeout);
        });
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public Set<K> keySet() {
        Collection<K> list = new ArrayList<>();
        entries.forEach(entry -> {
            list.add((K) entry.getKey());
        });
        return list.stream().collect(Collectors.toSet());
    }

    @Override
    public Collection<V> values() {
        Collection<V> list = new ArrayList<>();
        entries.forEach(entry -> {
            list.add((V) entry.getValue());
        });
        return list;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        final V v = get(key);
        return (v == null) ? defaultValue : v;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        HashMap<K, V> map = new HashMap<>();
        entries.forEach(entry -> {
            map.put((K) entry.getKey(), (V) entry.getValue());
        });
        map.forEach(action);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return remove(value) != null;
    }

    private static class Entry<K, V> {
        private final K key;
        private final V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}
