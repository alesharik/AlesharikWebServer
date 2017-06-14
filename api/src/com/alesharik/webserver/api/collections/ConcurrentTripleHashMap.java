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
import one.nio.lock.RWLock;

import java.util.Map;

/**
 * The concurrent version of {@link TripleHashMap}
 */
public class ConcurrentTripleHashMap<K, V, A> extends TripleHashMap<K, V, A> implements Cloneable {
    private final RWLock lock = new RWLock();

    @Override
    public int size() {
        try {
            lock.lockRead();
            return super.size();
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public boolean isEmpty() {
        try {
            lock.lockRead();
            return super.isEmpty();
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public V get(Object key) {
        try {
            lock.lockRead();
            return super.get(key);
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public A getAddition(Object key) {
        try {
            lock.lockRead();
            return super.getAddition(key);
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        try {
            lock.lockRead();
            return super.containsKey(key);
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        try {
            lock.lockRead();
            return super.containsValue(value);
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public boolean containsAddition(Object addition) {
        try {
            lock.lockRead();
            return super.containsAddition(addition);
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public Object put(K key, V value, A addition) {
        try {
            lock.lockWrite();
            return putUnsafe(key, value, addition);
        } finally {
            lock.unlockWrite();
        }
    }

    private Object putUnsafe(K key, V value, A addition) {
        return super.put0(key, value, addition);
    }

    @Override
    public void putAll(Map<K, V> m) {
        try {
            lock.lockWrite();
            m.forEach((k, v) -> putUnsafe(k, v, null));
        } finally {
            lock.unlockWrite();
        }
    }

    @Override
    public void putAll(TripleHashMap<K, V, A> map) {
        try {
            lock.lockWrite();
            map.forEach(this::putUnsafe);
        } finally {
            lock.unlockWrite();
        }
    }

    @Override
    public V remove(K key) {
        try {
            if(!containsKey(key)) {
                return null;
            }
            lock.lockWrite();
            return super.remove0(key);
        } finally {
            lock.unlockWrite();
        }
    }

    @Override
    public void clear() {
        lock.lockWrite();
        super.clear();
        lock.unlockWrite();
    }

    @Override
    public void forEach(TripleConsumer<K, V, A> action) {
        try {
            lock.lockRead();
            super.forEach(action);
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public TripleHashMap clone() throws CloneNotSupportedException {
        try {
            lock.lockRead();
            return super.clone();
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public boolean equals(Object o) {
        try {
            lock.lockRead();
            return super.equals(o);
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public int hashCode() {
        try {
            lock.lockRead();
            return super.hashCode();
        } finally {
            lock.unlockRead();
        }
    }

    @Override
    public String toString() {
        try {
            lock.lockRead();
            return super.toString();
        } finally {
            lock.unlockRead();
        }
    }
}
