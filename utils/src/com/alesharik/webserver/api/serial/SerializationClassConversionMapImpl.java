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

package com.alesharik.webserver.api.serial;

import com.alesharik.webserver.exception.error.UnexpectedBehaviorError;
import lombok.Data;
import org.apache.commons.collections4.SortedBidiMap;
import org.apache.commons.collections4.bidimap.DualTreeBidiMap;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.locks.StampedLock;

final class SerializationClassConversionMapImpl implements SerializationClassConversionMap {
    private final SortedBidiMap<Long, ComparableClass> map = new DualTreeBidiMap<>();
    private final StampedLock lock = new StampedLock();

    @Override
    public void addConversion(long id, @Nonnull Class<?> clazz) {
        long l = lock.writeLock();
        try {
            if(map.putIfAbsent(id, new ComparableClass(clazz)) != null)
                throw new IllegalStateException("Id " + id + " already exists!");
        } finally {
            lock.unlockWrite(l);
        }
    }

    @Override
    public long addConversion(@Nonnull Class<?> clazz) {
        long l = lock.writeLock();
        long id = map.isEmpty() ? 0 : map.lastKey() + 1;
        try {
            if(map.putIfAbsent(id, new ComparableClass(clazz)) != null)
                throw new UnexpectedBehaviorError("Id " + id + " already exists but it shouldn't!");
        } finally {
            lock.unlockWrite(l);
        }
        return id;
    }

    @Override
    public long getConversionFor(Class<?> clazz) {
        long l = lock.tryOptimisticRead();

        ComparableClass value = new ComparableClass(clazz);
        Long ret = map.getKey(value);
        if(!lock.validate(l)) {
            l = lock.readLock();
            try {
                ret = map.getKey(value);
            } finally {
                lock.unlockRead(l);
            }
        }

        return ret == null ? -1 : ret;
    }

    @Override
    public long getNextId() {
        long l = lock.tryOptimisticRead();

        long id = map.isEmpty() ? 0 : map.lastKey() + 1;
        if(!lock.validate(l)) {
            l = lock.readLock();
            try {
                id = map.isEmpty() ? 0 : map.lastKey() + 1;
            } finally {
                lock.unlockRead(l);
            }
        }

        return id;
    }

    @Nullable
    @Override
    public Class<?> resolveConversion(long id) {
        long l = lock.tryOptimisticRead();

        ComparableClass ret = map.get(id);
        if(!lock.validate(l)) {
            l = lock.readLock();
            try {
                ret = map.get(id);
            } finally {
                lock.unlockRead(l);
            }
        }

        return ret == null ? null : ret.getClazz();
    }

    @Override
    public long getOrCreateConversionFor(Class<?> clazz) {
        long l = lock.tryOptimisticRead();

        ComparableClass value = new ComparableClass(clazz);
        Long ret = map.getKey(value);
        if(ret == null) {
            long l1 = lock.writeLock();
            try {
                ret = map.getKey(value);
                if(ret != null)
                    return ret;
                long id = map.isEmpty() ? 0 : map.lastKey() + 1;
                map.put(id, value);
                return id;
            } finally {
                lock.unlockWrite(l1);
            }
        }
        if(!lock.validate(l)) {
            l = lock.readLock();
            try {
                ret = map.getKey(clazz);
                if(ret == null) {
                    l = lock.tryConvertToWriteLock(l);
                    ret = map.getKey(value);
                    if(ret != null)
                        return ret;
                    long id = map.isEmpty() ? 0 : map.lastKey() + 1;
                    map.put(id, value);
                    return id;
                }
            } finally {
                lock.unlock(l);
            }
        }

        return ret;
    }

    void cleanClassesFromClassLoader(ClassLoader loader) {
        long l = lock.writeLock();
        try {
            map.values().removeIf(comparableClass -> comparableClass.getClazz().getClassLoader() == loader);
        } finally {
            lock.unlockWrite(l);
        }
    }

    @Data
    private static final class ComparableClass implements Comparable<ComparableClass> {
        private final Class<?> clazz;

        @Override
        public int compareTo(@NotNull SerializationClassConversionMapImpl.ComparableClass o) {
            return Integer.compare(clazz.hashCode(), o.clazz.hashCode());
        }
    }
}
