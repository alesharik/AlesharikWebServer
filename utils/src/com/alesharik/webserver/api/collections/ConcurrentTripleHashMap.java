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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * Concurrent version of {@link TripleHashMap}
 */
//FIXME move to per-element fast ExponentialBackoff read/write locks
public class ConcurrentTripleHashMap<K, V, A> extends TripleHashMap<K, V, A> implements Cloneable {
    private static final long serialVersionUID = 2975616746052721434L;

    protected transient final ReadWriteLock lock = new ReentrantReadWriteLock(false);

    @Nullable
    @Override
    public V get(@Nullable K key) {
        if(key == null)
            return null;

        try {
            lock.readLock().lock();
            return super.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Nullable
    @Override
    public A getAddition(@Nullable K key) {
        if(key == null)
            return null;

        try {
            lock.readLock().lock();
            return super.getAddition(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean containsKey(@Nullable K key) {
        if(key == null)
            return false;

        try {
            lock.readLock().lock();
            return super.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean containsValue(@Nullable V value) {
        if(value == null)
            return false;

        try {
            lock.readLock().lock();
            return super.containsValue(value);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean containsAddition(@Nullable A addition) {
        if(addition == null)
            return false;

        try {
            lock.readLock().lock();
            return super.containsAddition(addition);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Nullable
    @Override
    public Triple<K, V, A> put(@Nonnull K key, @Nonnull V value, @Nullable A addition) {
        try {
            lock.writeLock().lock();
            return super.put(key, value, addition);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void putAll(@Nonnull Map<K, V> m) {
        try {
            lock.writeLock().lock();
            super.putAll(m);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void putAll(@Nonnull TripleHashMap<K, V, A> map) {
        try {
            lock.writeLock().lock();
            super.putAll(map);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Nullable
    @Override
    public Triple<K, V, A> remove(@Nullable K key) {
        if(key == null)
            return null;
        try {
            lock.writeLock().lock();
            return super.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        try {
            lock.writeLock().lock();
            super.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Nonnull
    @Override
    public Set<Triple<K, V, A>> entrySet() {
        try {
            lock.readLock().lock();
            return super.entrySet();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Nullable
    @Override
    protected TripleHashMap.Entry<K, V, A> getEntry(@Nullable K key) {
        try {
            lock.readLock().lock();
            return super.getEntry(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean replace(@Nonnull K key, @Nonnull V oldValue, @Nonnull V newValue) {
        Entry<K, V, A> entry = getEntry(key);
        if(entry == null)
            return false;

        try {
            lock.readLock().lock();
            if(!oldValue.equals(entry.getValue()))
                return false;
        } finally {
            lock.readLock().unlock();
        }

        try {
            lock.writeLock().lock();
            entry.setValue(newValue);
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue, A addition) {
        Entry<K, V, A> entry = getEntry(key);
        if(entry == null)
            return false;

        try {
            lock.readLock().lock();
            if(!oldValue.equals(entry.getValue()))
                return false;
        } finally {
            lock.readLock().unlock();
        }

        try {
            lock.writeLock().lock();
            entry.setValue(newValue);
            entry.setAddition(addition);
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }

    @Override
    public Triple<K, V, A> computeIfAbsent(@Nonnull K key, @Nonnull Function<? super K, ? extends V> mappingFunction, @Nonnull Function<? super K, ? extends A> additionalFunction) {
        boolean contains;
        V value;
        A addition;
        try {
            lock.readLock().lock();
            contains = !super.containsKey(key);

            if(contains) {
                value = mappingFunction.apply(key);
                addition = additionalFunction.apply(key);
                super.putActual(key, value, addition);
                return Triple.immutable(key, value, addition);
            } else
                return super.getEntry(key).mimic();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Nullable
    @Override
    public Triple<K, V, A> computeIfPresent(@Nonnull K key, @Nonnull TripleFunction<? super K, ? super V, ? super A, ? extends V> remappingFunction, @Nonnull TripleFunction<? super K, ? super V, ? super A, ? extends A> additionalFunction) {
        Entry<K, V, A> entry = getEntry(key);
        if(entry == null)
            return null;
        else {
            try {
                lock.writeLock().lock();
                entry.setValue(remappingFunction.apply(entry.getKey(), entry.getValue(), entry.getAddition()));
                entry.setAddition(additionalFunction.apply(entry.getKey(), entry.getValue(), entry.getAddition()));
            } finally {
                lock.writeLock().unlock();
            }
            return entry.mimic();
        }
    }

    @Nonnull
    @Override
    public Triple<K, V, A> compute(@Nonnull K key, @Nonnull TripleFunction<? super K, ? super V, ? super A, ? extends V> remappingFunction, @Nonnull TripleFunction<? super K, ? super V, ? super A, ? extends A> additionalFunction) {
        try {
            lock.writeLock().lock();

            Entry<K, V, A> entry = super.getEntry(key);
            if(entry == null) {
                V val = remappingFunction.apply(key, null, null);
                A addition = additionalFunction.apply(key, val, null);
                super.put(key, val, addition);
                return Triple.immutable(key, val, addition);
            } else {
                entry.setValue(remappingFunction.apply(key, entry.getValue(), entry.getAddition()));
                entry.setAddition(additionalFunction.apply(key, entry.getValue(), entry.getAddition()));
                return entry.mimic();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Nonnull
    @Override
    public Triple<K, V, A> merge(@Nonnull K key, @Nonnull V value, @Nonnull A addition, @Nonnull TripleFunction<? super K, ? super V, ? super A, ? extends V> remappingFunction, @Nonnull TripleFunction<? super K, ? super V, ? super A, ? extends A> additionalFunction) {
        try {
            lock.writeLock().lock();
            if(!super.containsKey(key)) {
                super.put(key, value, addition);
                return Triple.immutable(key, value, addition);
            } else
                return super.compute(key, remappingFunction, additionalFunction);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void forEach(@Nonnull TripleConsumer<K, V, A> action) {
        try {
            lock.readLock().lock();
            super.forEach(action);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void replaceAll(@Nonnull TripleFunction<? super K, ? super V, ? super A, ? extends V> remappingFunction, @Nonnull TripleFunction<? super K, ? super V, ? super A, ? extends A> additionalFunction) {
        try {
            lock.writeLock().lock();
            super.replaceAll(remappingFunction, additionalFunction);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public TripleHashMap<K, V, A> clone() {
        try {
            lock.readLock().lock();
            return super.clone();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public String toString() {
        try {
            lock.readLock().lock();
            return super.toString();
        } finally {
            lock.readLock().unlock();
        }
    }
}
