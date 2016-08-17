package com.alesharik.webserver.api;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * WARNING!This class is not thread-safe!
 * This list updates only on method call
 */
public class CachedArrayList<V> extends AbstractList<V>
        implements List<V>, RandomAccess, Cloneable, java.io.Serializable {

    private final HashMap<V, Long> cachedArrayList;
    private long oldTime;

    public CachedArrayList() {
        this.oldTime = new Date().getTime();
        cachedArrayList = new HashMap<>();
    }

    public CachedArrayList(int capacity) {
        cachedArrayList = new HashMap<>(capacity);
    }

    public void add(V v, long timeout) {
        update();
        cachedArrayList.put(v, timeout);
        update();
    }

    public void delete(V value) {
        cachedArrayList.remove(value);
    }

    @Override
    public V get(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        cachedArrayList.clear();
    }

    public void addAll(Collection<? extends V> c, long timeout) {
        update();
        c.forEach(item -> cachedArrayList.put(item, timeout));
        update();
    }

    @Override
    public Iterator<V> iterator() {
        update();
        return cachedArrayList.keySet().iterator();
    }

    @Override
    public boolean isEmpty() {
        update();
        return cachedArrayList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        update();
        return cachedArrayList.keySet().contains(o);
    }

    @Override
    public Object[] toArray() {
        update();
        return cachedArrayList.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        update();
        return cachedArrayList.keySet().toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        update();
        return cachedArrayList.keySet().containsAll(c);
    }

    @Override
    public Spliterator<V> spliterator() {
        update();
        return cachedArrayList.keySet().spliterator();
    }

    @Override
    public Stream<V> stream() {
        update();
        return cachedArrayList.keySet().stream();
    }

    @Override
    public Stream<V> parallelStream() {
        update();
        return cachedArrayList.keySet().parallelStream();
    }

    @Override
    public void forEach(Consumer<? super V> action) {
        update();
        cachedArrayList.keySet().forEach(action);
    }

    @Override
    public int size() {
        update();
        return cachedArrayList.size();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        CachedArrayList<?> that = (CachedArrayList<?>) o;

        return cachedArrayList.equals(that.cachedArrayList);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + cachedArrayList.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CachedArrayList{" +
                "cachedArrayList=" + cachedArrayList +
                '}';
    }

    private void update() {
        long newTime = new Date().getTime();
        long delta = newTime - oldTime;
        cachedArrayList.forEach((v, time) -> {
            long nextTime = time - delta;
            if(nextTime < 0) {
                cachedArrayList.remove(v);
            } else {
                cachedArrayList.put(v, nextTime);
            }
        });
        oldTime = newTime;
    }

}
