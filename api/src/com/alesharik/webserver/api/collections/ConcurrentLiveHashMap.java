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
import com.alesharik.webserver.api.ticking.OneThreadTickingPool;
import com.alesharik.webserver.api.ticking.Tickable;
import com.alesharik.webserver.api.ticking.TickingPool;
import one.nio.lock.RWLock;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This is a concurrent hash map. Every key in this map has an expiry period. If life time of key is 0 then key removed.
 * Map updates every set delay in {@link TickingPool}(default or not) => concurrently
 */
//TODO write configurable pool
public class ConcurrentLiveHashMap<K, V> extends HashMapWrapper<K, V> implements Tickable, Cloneable {
    private static final OneThreadTickingPool DEFAULT_POOL = new OneThreadTickingPool();
    private static final long DEFAULT_LIFE_TIME = 60 * 1000;
    private static final long DEFAULT_DELAY = 1000;
    private static final int DEFAULT_CAPACITY = 16;

    private final AtomicBoolean isStarted;
    private final long delay;
    private final RWLock lock;

    //    private TickingPool pool;
    private TripleHashMap<K, V, Long> map;

    /**
     * Initialize {@link ConcurrentLiveHashMap} with <code>DEFAULT_DELAY</code> and <code>DEFAULT_CAPACITY</code> and start it
     */
    public ConcurrentLiveHashMap() {
        this(DEFAULT_DELAY, DEFAULT_CAPACITY);
    }

    /**
     * Initialize {@link ConcurrentLiveHashMap} with <code>DEFAULT_DELAY</code> and start it
     *
     * @param count initial capacity
     */
    public ConcurrentLiveHashMap(int count) {
        this(DEFAULT_DELAY, count);
    }

    /**
     * Initialize {@link ConcurrentLiveHashMap} with <code>DEFAULT_CAPACITY</code> and start it
     *
     * @param delay update delay in milliseconds
     */
    public ConcurrentLiveHashMap(long delay) {
        this(delay, DEFAULT_CAPACITY);
    }

    /**
     * Initialize {@link ConcurrentLiveHashMap} and start it
     *
     * @param delay update delay in milliseconds
     * @param count initial capacity
     */
    public ConcurrentLiveHashMap(long delay, int count) {
        this(delay, count, true);
    }

    /**
     * Initialize {@link ConcurrentLiveHashMap}
     *
     * @param delay     update delay in milliseconds
     * @param count     initial capacity
     * @param autoStart if it is true then map starts automatically, otherwise it is need to start manually(<code>map.start()</code>)
     */
    public ConcurrentLiveHashMap(long delay, int count, boolean autoStart) {
        if(delay < 1) {
            throw new IllegalArgumentException("Delay can't be < 1!");
        }
        if(count < 0) {
            throw new IllegalArgumentException("Count cn't be < 0!");
        }

        this.isStarted = new AtomicBoolean(false);
        this.lock = new RWLock();
        map = new TripleHashMap<>(count);
        this.delay = delay;
        if(autoStart) {
            start();
        }
    }

    @Override
    public int size() {
        try {
            lock.lockRead();
            return map.size();
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public boolean isEmpty() {
        try {
            lock.lockRead();
            return map.isEmpty();
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        try {
            lock.lockRead();
            return map.containsValue(value);
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        try {
            lock.lockRead();
            return map.containsKey(key);
        } finally {
            lock.unlockRead();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        try {
            lock.lockRead();
            return map.get(key);
        } finally {
            lock.unlockRead();
        }
    }

    public long getTime(Object key) {
        try {
            lock.lockRead();
            return map.getAddition(key);
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public V put(K key, V value) {
        return put(key, value, DEFAULT_LIFE_TIME);
    }

    @SuppressWarnings("unchecked")
    public V put(K key, V value, long time) {
        try {
            lock.lockWrite();
            return (V) map.put(key, value, time);
        } finally {
            lock.unlockWrite();
        }
    }

    private void put0(K key, V value, long time) {
        map.put(key, value, time);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
        try {
            lock.lockWrite();
            return map.remove((K) key);
        } finally {
            lock.unlockWrite();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        putAll(m, DEFAULT_LIFE_TIME);
    }

    public void putAll(Map<? extends K, ? extends V> m, long time) {
        try {
            lock.lockWrite();
            m.forEach((o, o2) -> put0(o, o2, time));
        } finally {
            lock.unlockWrite();
        }
    }

    public void putAll(TripleHashMap<? extends K, ? extends V, ? extends Long> map) {
        try {
            lock.lockWrite();
            this.map.putAll((TripleHashMap<K, V, Long>) map);
        } finally {
            lock.unlockWrite();
        }
    }

    @Override
    public void clear() {
        try {
            lock.lockWrite();
            map.clear();
        } finally {
            lock.unlockWrite();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keySet() {
        try {
            lock.lockRead();
            return map.keySet();
        } finally {
            lock.unlockRead();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> values() {
        try {
            lock.lockRead();
            return map.values();
        } finally {
            lock.unlockRead();
        }
    }

    @SuppressWarnings("unchecked")
    public Collection<Long> times() {
        try {
            lock.lockRead();
            return map.additions();
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof ConcurrentLiveHashMap)) return false;
        if(!super.equals(o)) return false;

        ConcurrentLiveHashMap<?, ?> that = (ConcurrentLiveHashMap<?, ?>) o;

        try {
            lock.lockRead();
            if(delay != that.delay) return false;
//            if(pool != null ? !pool.equals(that.pool) : that.pool != null) return false;
            return map != null ? map.equals(that.map) : that.map == null;
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public int hashCode() {
        try {
            lock.lockRead();
            int result = super.hashCode();
            result = 31 * result + (int) (delay ^ (delay >>> 32));
//            result = 31 * result + (pool != null ? pool.hashCode() : 0);
            result = 31 * result + (map != null ? map.hashCode() : 0);
            return result;
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public String toString() {
        try {
            lock.lockRead();
            return "ConcurrentLiveHashMap{" +
                    "isStarted=" + isStarted +
                    ", delay=" + delay +
//                    ", pool=" + pool +
                    ", map=" + map +
                    '}';
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    //TODO check if it works
    public Object clone() throws CloneNotSupportedException {
        try {
            lock.lockRead();
            ConcurrentLiveHashMap map = ((ConcurrentLiveHashMap) super.clone());
            map.map = this.map;
//            map.pool = pool;
            return map;
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        try {
            lock.lockRead();
            Set<Entry<K, V>> entries = new HashSet<>(size());
            forEach((k, v) -> entries.add(new AbstractMap.SimpleEntry<>(k, v)));
            return entries;
        } finally {
            lock.unlockRead();
        }
    }

    public Set<Triple> triple() {
        try {
            lock.lockRead();
            return map.entrySet();
        } finally {
            lock.unlockRead();
        }
    }

    public void setTime(K k, long time) {
        try {
            lock.lockWrite();
            map.put(k, map.get(k), time);
        } finally {
            lock.unlockWrite();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V getOrDefault(Object key, V defaultValue) {
        try {
            lock.lockRead();
            return (V) map.getOrDefault((K) key, defaultValue);
        } finally {
            lock.unlockRead();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        try {
            lock.lockRead();
            map.forEach((o, o2, o3) -> action.accept(o, o2));
        } finally {
            lock.unlockRead();
        }
    }

    public void forEach(TripleConsumer<? extends K, ? extends V, ? super Long> consumer) {
        forEach0(consumer);
    }

    private void forEach0(TripleConsumer<? extends K, ? extends V, ? super Long> consumer) {
        try {
            lock.lockRead();
            map.forEach((TripleConsumer<K, V, Long>) consumer);
        } finally {
            lock.unlockRead();
        }
    }

    //TODO there are deadlocks!

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
        return (V) map.putIfAbsent(key, value, time);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return map.remove((K) key, (V) value);
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
    public V computeIfPresent(K key, TripleFunction<? super K, ? super V, ? super Long, ? extends V> remappingFunction,
                              TripleFunction<? super K, ? super V, ? super Long, ? extends Long> addition) {
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
        return (V) map.merge(key, value, time, remappingFunction, additionalFunction);
    }

    void updateMap(long period) {
        try {
            lock.lockWrite();
            map.forEach((k, v, aLong) -> {
                long current = aLong - period;
                if(current <= 0) {
                    map.remove(k);
                } else {
                    map.replace(k, v, v, current);
                }
            });
        } finally {
            lock.unlockWrite();
        }
    }

    public void start() {
//        if(!isStarted) {
//            TickingPool.addHashMap(this, delay);
//            isStarted = true;
//        }
        if(!isStarted.get()) {
            DEFAULT_POOL.startTicking(this, delay);
            isStarted.set(true);
        }
    }

    public void stop() {
//        if(isStarted) {
//            TickingPool.removeHashMap(this, delay);
//            isStarted = false;
//        }
        if(isStarted.get()) {
            DEFAULT_POOL.stopTicking(this);
            isStarted.set(false);
        }
    }

    @Override
    public void tick() throws Exception {
        updateMap(delay);
    }
}
