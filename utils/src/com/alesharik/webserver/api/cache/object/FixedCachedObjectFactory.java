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

package com.alesharik.webserver.api.cache.object;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.StampedLock;

/**
 * This factory type contains fixed number of object. If object count >= max object count, objects put by {@link #putInstance(Recyclable)}
 * will be thrown away. If there is no objects in the cache when invoking {@link #getInstance()}, new object will be created
 * @param <T> containing object type
 */
@ThreadSafe
public class FixedCachedObjectFactory<T extends Recyclable> implements CachedObjectFactory<T> {
    protected final StampedLock stampedLock;
    protected final int maxCount;
    protected final ObjectFactory<T> factory;
    protected final List<T> cache = new ArrayList<>();

    /**
     * Creates new factory
     * @param maxCount maximum object count
     * @param factory the object factory
     */
    public FixedCachedObjectFactory(int maxCount, @Nonnull ObjectFactory<T> factory) {
        this.maxCount = maxCount;
        this.factory = factory;
        this.stampedLock = new StampedLock();
        refill();
    }

    @Override
    public T getInstance() {
        long lock = stampedLock.readLock();
        try {
            if(cache.size() == 0)
                return factory.newInstance();
            else {
                long write = stampedLock.tryConvertToWriteLock(lock);
                if(!stampedLock.validate(write)) {
                    stampedLock.unlock(lock);
                    lock = stampedLock.writeLock();
                } else
                    lock = write;

                return cache.remove(0);
            }
        } finally {
            stampedLock.unlock(lock);
        }
    }

    @Override
    public void putInstance(@Nonnull T i) {
        long lock = stampedLock.writeLock();
        try {
            i.recycle();
            if(maxCount >= cache.size())
                cache.add(i);
        } finally {
            stampedLock.unlockWrite(lock);
        }
    }

    @Override
    public void refill() {
        long lock = stampedLock.readLock();
        try {
            int size = cache.size();
            if(size > maxCount)
                return;

            long writeLock = stampedLock.tryConvertToWriteLock(lock);
            if(!stampedLock.validate(writeLock)) {
                stampedLock.unlockRead(lock);
                lock = stampedLock.writeLock();
            } else
                lock = writeLock;

            while(cache.size() < maxCount) {
                cache.add(factory.newInstance());
            }
        } finally {
            stampedLock.unlock(lock);
        }
    }

    @Override
    public int getMaxCachedObjectCount() {
        return maxCount;
    }

    @Override
    public int getMinCachedObjectCount() {
        return 0;
    }

    @Override
    public int getCurrentCachedObjectCount() {
        long l = stampedLock.tryOptimisticRead();
        int size = cache.size();
        if(!stampedLock.validate(l)) {
            l = stampedLock.readLock();
            try {
                size = cache.size();
            } finally {
                stampedLock.unlockRead(l);
            }
        }
        return size;
    }
}
