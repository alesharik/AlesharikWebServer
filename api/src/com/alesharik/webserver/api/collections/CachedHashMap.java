package com.alesharik.webserver.api.collections;

import com.alesharik.webserver.api.functions.TripleConsumer;
import com.alesharik.webserver.api.functions.TripleFunction;
import com.alesharik.webserver.api.misc.Triple;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This map updates only on method call.
 * If method has no lifeTime, it set DEFAULT_LIFE_TIME as lifeTime
 */
public class CachedHashMap<K, V> extends HashMapWrapper<K, V> {
    private static final long DEFAULT_LIFE_TIME = 60 * 1000;

    private TripleHashMap<K, V, Long> map;
    private long lastTime;

    public CachedHashMap() {
        map = new TripleHashMap<>();
        lastTime = System.currentTimeMillis();
    }

    public CachedHashMap(int count) {
        map = new TripleHashMap<>(count);
        lastTime = System.currentTimeMillis();
    }

    @Override
    public int size() {
        update();
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        update();
        return map.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        update();
        return map.containsValue(value);
    }

    @Override
    public boolean containsKey(Object key) {
        update();
        return map.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        update();
        return (V) map.get(key);
    }

    public long getTime(Object key) {
        update();
        return (long) map.getAddition(key);
    }

    @Override
    public V put(K key, V value) {
        return put(key, value, DEFAULT_LIFE_TIME);
    }

    @SuppressWarnings("unchecked")
    public V put(K key, V value, long time) {
        return (V) map.put(key, value, time);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
        update();
        return map.remove((K) key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        putAll(m, DEFAULT_LIFE_TIME);
    }

    public void putAll(Map<? extends K, ? extends V> m, long time) {
        m.forEach((o, o2) -> put(o, o2, time));
    }

    public void putAll(TripleHashMap<? extends K, ? extends V, ? extends Long> map) {
        this.map.putAll((TripleHashMap<K, V, Long>) map);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keySet() {
        update();
        return map.keySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> values() {
        update();
        return map.values();
    }

    @SuppressWarnings("unchecked")
    public Collection<Long> times() {
        update();
        return map.additions();
    }

    @Override
    public boolean equals(Object o) {
        update();
        if(this == o) return true;
        if(!(o instanceof CachedHashMap)) return false;

        CachedHashMap<?, ?> that = (CachedHashMap<?, ?>) o;

        return map != null ? map.equals(that.map) : that.map == null;

    }

    @Override
    public int hashCode() {
        update();
        int result = 0;
        result = 31 * result + (map != null ? map.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        update();
        return "CachedHashMap{" +
                "map=" + map +
                '}';
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        update();
        CachedHashMap map = ((CachedHashMap) super.clone());
        map.map = this.map;
        return map;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        update();
        Set<Entry<K, V>> entries = new HashSet<>(size());
        forEach((k, v) -> entries.add(new AbstractMap.SimpleEntry<>(k, v)));
        return entries;
    }

    public Set<Triple> triple() {
        update();
        return map.entrySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public V getOrDefault(Object key, V defaultValue) {
        update();
        return (V) map.getOrDefault((K) key, defaultValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        update();
        map.forEach((o, o2, o3) -> action.accept((K) o, (V) o2));
    }

    public void forEach(TripleConsumer<? extends K, ? extends V, ? super Long> consumer) {
        update();
        forEach0(consumer);
    }

    private void forEach0(TripleConsumer<? extends K, ? extends V, ? super Long> consumer) {
        map.forEach((TripleConsumer<K, V, Long>) consumer);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        forEach((k, v) -> replace(k, v, function.apply(k, v)));
    }

    public void replaceAll(TripleFunction<? super K, ? super V, ? super Long, ? super V> function) {
        forEach((k, v, aLong) -> replace0(k, v, (V) function.apply(k, v, aLong), aLong));
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return putIfAbsent(key, value, DEFAULT_LIFE_TIME);
    }

    @SuppressWarnings("unchecked")
    public V putIfAbsent(K key, V value, long time) {
        update();
        return (V) map.putIfAbsent(key, value, time);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return map.remove((K) key, (V) value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        update();
        return map.replace(key, oldValue, newValue);
    }

    public boolean replace(K key, V oldValue, V newValue, long time) {
        update();
        return replace0(key, oldValue, newValue, time);
    }

    private boolean replace0(K key, V oldValue, V newValue, long time) {
        return map.replace(key, oldValue, newValue, time);
    }

    @Override
    public V replace(K key, V value) {
        update();
        V old = get(key);
        if(old != null) {
            map.replace(key, old, value);
            return old;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        update();
        return (V) map.computeIfAbsent(key, mappingFunction, o -> map.getAddition(key));
    }

    @SuppressWarnings("unchecked")
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, Function additionalFunction) {
        update();
        return (V) map.computeIfAbsent(key, mappingFunction, additionalFunction);
    }

    /**
     * Can't realise. Use <code>computeIfPresent(K, TripleFunction<? super K, ? super V, ? extends Long, ? extends V>,
     * TripleFunction<? super K, ? super V, ? extends Long, ? extends Long>)</code>
     */
    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public V computeIfPresent(K key, TripleFunction<? super K, ? super V, ? super Long, ? extends V> remappingFunction,
                              TripleFunction<? super K, ? super V, ? super Long, ? extends Long> addition) {
        update();
        return (V) map.computeIfPresent(key, remappingFunction, addition);
    }

    /**
     * Can't realise. Use <code>compute(K key, TripleFunction<? super K, ? super V, ? extends Long, ? extends V>,
     * TripleFunction<? super K, ? super V, ? extends Long, ? extends Long>)</code>
     */
    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public V compute(K key, TripleFunction<? super K, ? super V, ? super Long, ? extends V> remappingFunction,
                     TripleFunction<? super K, ? super V, ? super Long, ? extends Long> addition) {
        update();
        return (V) map.compute(key, remappingFunction, addition);
    }

    /**
     * Can't realize. Use <code>merge(K, K, Long, TripleFunction<? extends K, ? extends V, ? extends Long, ? extends V>,
     * TripleFunction<? extends K, ? extends V, ? extends Long, ? extends Long>)</code>
     */
    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public V merge(K key, V value, Long time, TripleFunction<? super K, ? super V, ? super Long, ? extends V> remappingFunction,
                   TripleFunction<? super K, ? super V, ? super Long, ? extends Long> additionalFunction) {
        update();
        return (V) map.merge(key, value, time, remappingFunction, additionalFunction);
    }

    private void update() {
        long current = System.currentTimeMillis();
        updateMap(current - lastTime);
        lastTime = current;
    }

    private void updateMap(long delta) {
        forEach0((k, v, aLong) -> {
            long current = aLong - delta;
            if(current < 0) {
                remove(k);
            } else {
                replace0(k, v, v, current);
            }
        });
    }
}
