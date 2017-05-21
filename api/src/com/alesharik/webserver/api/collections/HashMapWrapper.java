package com.alesharik.webserver.api.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Extends this class to create {@link Map} with builtin {@link HashMap}
 */
public class HashMapWrapper<K, V> implements Map<K, V>, Cloneable, Serializable {
    private final HashMap<K, V> hashMap;

    public HashMapWrapper() {
        hashMap = new HashMap<>();
    }

    public HashMapWrapper(int count) {
        hashMap = new HashMap<>(count);
    }

    public HashMapWrapper(HashMap<K, V> map) {
        hashMap = new HashMap<>();
        hashMap.putAll(map);
    }

    @Override
    public int size() {
        return hashMap.size();
    }

    @Override
    public boolean isEmpty() {
        return hashMap.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        return hashMap.containsValue(value);
    }

    @Override
    public boolean containsKey(Object key) {
        return hashMap.containsKey(key);
    }

    @Override
    public V get(Object key) {
        return hashMap.get(key);
    }

    @Override
    public V put(K key, V value) {
        return hashMap.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return hashMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        hashMap.putAll(m);
    }

    @Override
    public void clear() {
        hashMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return hashMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return hashMap.values();
    }

    @Override
    public boolean equals(Object o) {
        return hashMap.equals(o);
    }

    @Override
    public int hashCode() {
        return hashMap.hashCode();
    }

    @Override
    public String toString() {
        return hashMap.toString();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return hashMap.entrySet();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return hashMap.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        hashMap.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        hashMap.replaceAll(function);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return hashMap.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return hashMap.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return hashMap.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return hashMap.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return hashMap.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return hashMap.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return hashMap.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return hashMap.merge(key, value, remappingFunction);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() throws CloneNotSupportedException {
        HashMap<K, V> map = (HashMap<K, V>) super.clone();
        map.putAll(hashMap);
        return map;
    }
}
