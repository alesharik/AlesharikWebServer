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

public class LiveHashMap<K, V> extends HashMapWrapper<K, V> {
    private static final long DEFAULT_LIFE_TIME = 60 * 1000;
    private static final long DEFAULT_DELAY = 1000;
    private static final int DEFAULT_CAPACITY = 16;

    private TripleHashMap<K, V, Long> map;
    private long delay;
    private boolean isStarted = false;

    public LiveHashMap() {
        this(DEFAULT_DELAY, DEFAULT_CAPACITY);
    }

    public LiveHashMap(int count) {
        this(DEFAULT_DELAY, count);
    }

    public LiveHashMap(long delay) {
        this(delay, DEFAULT_CAPACITY);
    }

    public LiveHashMap(long delay, int count) {
        this(delay, count, true);
    }

    /**
     * @param delay
     * @param count
     * @param autoStart
     */
    public LiveHashMap(long delay, int count, boolean autoStart) {
        if(delay < 1) {
            throw new IllegalArgumentException("Delay can't be < 1!");
        }
        if(count < 0) {
            throw new IllegalArgumentException("Count cn't be < 0!");
        }

        map = new TripleHashMap<>(count);
        this.delay = delay;
        if(autoStart) {
            start();
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        return (V) map.get(key);
    }

    public long getTime(Object key) {
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
        return (V) map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        putAll(m, DEFAULT_LIFE_TIME);
    }

    public void putAll(Map<? extends K, ? extends V> m, long time) {
        m.forEach((o, o2) -> put(o, o2, time));
    }

    public void putAll(TripleHashMap<? extends K, ? extends V, ? extends Long> map) {
        this.map.putAll(map);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> values() {
        return map.values();
    }

    @SuppressWarnings("unchecked")
    public Collection<Long> times() {
        return map.additions();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof LiveHashMap)) return false;

        LiveHashMap<?, ?> that = (LiveHashMap<?, ?>) o;

        return map != null ? map.equals(that.map) : that.map == null;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (map != null ? map.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LiveHashMap{" +
                "map=" + map +
                '}';
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        LiveHashMap map = ((LiveHashMap) super.clone());
        map.map = this.map;
        return map;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entries = new HashSet<>(size());
        forEach((k, v) -> entries.add(new AbstractMap.SimpleEntry<>(k, v)));
        return entries;
    }

    public Set<Triple> triple() {
        return map.entrySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return (V) map.getOrDefault(key, defaultValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        map.forEach((o, o2, o3) -> action.accept((K) o, (V) o2));
    }

    public void forEach(TripleConsumer<? extends K, ? extends V, ? super Long> consumer) {
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
        return (V) map.putIfAbsent(key, value, true);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return map.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return map.replace(key, oldValue, newValue);
    }

    public boolean replace(K key, V oldValue, V newValue, long time) {
        return replace0(key, oldValue, newValue, time);
    }

    private boolean replace0(K key, V oldValue, V newValue, long time) {
        return map.replace(key, oldValue, newValue, time);
    }

    @Override
    public V replace(K key, V value) {
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
        return (V) map.computeIfAbsent(key, mappingFunction, o -> map.getAddition(key));
    }

    @SuppressWarnings("unchecked")
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction, Function additionalFunction) {
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
    public V computeIfPresent(K key, TripleFunction<? super K, ? super V, ? extends Long, ? extends V> remappingFunction,
                              TripleFunction<? super K, ? super V, ? extends Long, ? extends Long> addition) {
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
    public V compute(K key, TripleFunction<? super K, ? super V, ? extends Long, ? extends V> remappingFunction,
                     TripleFunction<? super K, ? super V, ? extends Long, ? extends Long> addition) {
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
    public V merge(K key, K value, Long time, TripleFunction<? extends K, ? extends V, ? extends Long, ? extends V> remappingFunction,
                   TripleFunction<? extends K, ? extends V, ? extends Long, ? extends Long> additionalFunction) {
        return (V) map.merge(key, value, time, remappingFunction, additionalFunction);
    }

    void updateMap(long period) {
        forEach0((k, v, aLong) -> {
            long current = aLong - period;
            if(current < 0) {
                remove(k);
            } else {
                replace(k, v, v, current);
            }
        });
    }

    public void start() {
        if(!isStarted) {
            TickingPool.addHashMap(this, delay);
            isStarted = true;
        }
    }

    public void stop() {
        if(isStarted) {
            TickingPool.removeHashMap(this, delay);
            isStarted = false;
        }
    }
}
