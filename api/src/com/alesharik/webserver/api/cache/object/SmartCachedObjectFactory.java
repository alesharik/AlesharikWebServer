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

import com.alesharik.webserver.api.statistics.FuzzyTimeCountStatistics;
import com.alesharik.webserver.api.statistics.TimeCountStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

public final class SmartCachedObjectFactory<T extends Recyclable> implements CachedObjectFactory<T> {
    private final ObjectFactory<T> factory;
    private final List<T> cache;
    private final AtomicInteger currentLimit;
    private final StampedLock stampedLock;

    private final TimeCountStatistics suppliedObjects;
    private final TimeCountStatistics retrievedObjects;
    private final TimeCountStatistics createdObjects;

    public SmartCachedObjectFactory(ObjectFactory<T> factory) {
        this.factory = factory;
        this.cache = new ArrayList<>();
        this.currentLimit = new AtomicInteger(16);
        this.stampedLock = new StampedLock();
        suppliedObjects = new FuzzyTimeCountStatistics(500, TimeUnit.MILLISECONDS);
        retrievedObjects = new FuzzyTimeCountStatistics(500, TimeUnit.MILLISECONDS);
        createdObjects = new FuzzyTimeCountStatistics(500, TimeUnit.MILLISECONDS);
        refill();
    }

    public SmartCachedObjectFactory(ObjectFactory<T> factory, long monitorTime, TimeUnit timeUnit) {
        this.factory = factory;
        this.cache = new ArrayList<>();
        this.currentLimit = new AtomicInteger(16);
        this.stampedLock = new StampedLock();
        suppliedObjects = new FuzzyTimeCountStatistics(monitorTime, timeUnit);
        retrievedObjects = new FuzzyTimeCountStatistics(monitorTime, timeUnit);
        createdObjects = new FuzzyTimeCountStatistics(monitorTime, timeUnit);
        refill();
    }

    @Override
    public T getInstance() {
        suppliedObjects.measure(1);
        long lock = stampedLock.readLock();
        try {
            if(cache.size() > 0) {
                long writeLock = stampedLock.tryConvertToWriteLock(lock);
                if(!stampedLock.validate(writeLock)) {
                    stampedLock.unlockRead(lock);
                    lock = stampedLock.writeLock();
                } else
                    lock = writeLock;
                retrievedObjects.measure(1);
                tryOptimise();
                return cache.remove(0);
            }
        } finally {
            stampedLock.unlock(lock);
        }
        createdObjects.measure(1);
        return factory.newInstance();
    }

    @Override
    public void putInstance(T i) {
        long lock = stampedLock.readLock();
        try {
            if(cache.size() <= currentLimit.get()) {
                long writeLock = stampedLock.tryConvertToWriteLock(lock);
                if(!stampedLock.validate(writeLock)) {
                    stampedLock.unlockRead(lock);
                    lock = stampedLock.writeLock();
                } else
                    lock = writeLock;
                i.recycle();
                cache.add(i);
                tryOptimise();
            }
        } finally {
            stampedLock.unlock(lock);
        }
    }

    @Override
    public void refill() {
        long lock = stampedLock.readLock();
        try {
            if(cache.size() <= currentLimit.get()) {
                long writeLock = stampedLock.tryConvertToWriteLock(lock);
                if(!stampedLock.validate(writeLock)) {
                    stampedLock.unlockRead(lock);
                    lock = stampedLock.writeLock();
                } else
                    lock = writeLock;
                int need = currentLimit.get() - cache.size();
                for(int i = 0; i < need; i++)
                    cache.add(factory.newInstance());
                tryOptimise();
            }
        } finally {
            stampedLock.unlock(lock);
        }
    }

    @Override
    public int getMaxCachedObjectCount() {
        return currentLimit.get();
    }

    @Override
    public int getMinCachedObjectCount() {
        return 0;
    }

    @Override
    public int getCurrentCachedObjectCount() {
        long lock = stampedLock.readLock();
        try {
            return cache.size();
        } finally {
            stampedLock.unlockRead(lock);
        }
    }

    private void tryOptimise() {
        if(createdObjects.getCount() > 5) {//Require more
            if(currentLimit.get() < createdObjects.getCount()) {
                int need = currentLimit.updateAndGet(operand -> operand + (int) (createdObjects.getCount() / 2)) - cache.size();
                for(int i = 0; i < need; i++)
                    cache.add(factory.newInstance());
            } else if(createdObjects.getCount() > retrievedObjects.getCount()) {//Require refill
                int need = currentLimit.get() - cache.size();
                for(int i = 0; i < need; i++)
                    cache.add(factory.newInstance());
            }
        } else if(currentLimit.get() > suppliedObjects.getCount() * 2) {
            currentLimit.getAndUpdate(operand -> operand / 2);
        }
    }
}
