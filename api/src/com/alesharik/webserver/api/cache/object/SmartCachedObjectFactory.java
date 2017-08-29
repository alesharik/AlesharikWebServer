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

import com.alesharik.webserver.api.statistics.PreciseConcurrentTimeCountStatistics;
import com.alesharik.webserver.api.statistics.TimeCountStatistics;
import lombok.Getter;
import sun.misc.Cleaner;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This object factory doesn't track cache object state and it is preferable to use {@link #putInstance(Recyclable)}
 *
 * @param <T>
 */
public final class SmartCachedObjectFactory<T extends Recyclable> implements CachedObjectFactory<T> {
    private static final int MAX_CREATE_COUNT = 100;

    private final ObjectFactory<T> factory;
    private final List<T> cache;

    private final TimeCountStatistics suppliedObjects;
    private final TimeCountStatistics retrievedObjects;
    private final TimeCountStatistics createdObjects;
    private final TaskImpl task;
    private final int defaultObjectCount;
    private final AtomicInteger maxObjectCount = new AtomicInteger();

    //====================Exponential backoff algorithm====================\\
    private final AtomicBoolean lock = new AtomicBoolean(false);
    private final AtomicBoolean putLock = new AtomicBoolean(false);

    public SmartCachedObjectFactory(ObjectFactory<T> factory) {
        this(factory, CachedObjectFactoryTaskExecutor.DEFAULT, 32);
    }

    public SmartCachedObjectFactory(ObjectFactory<T> factory, long monitorTime, TimeUnit timeUnit, int defaultObjectCount) {
        this(factory, monitorTime, timeUnit, CachedObjectFactoryTaskExecutor.DEFAULT, defaultObjectCount);
    }

    public SmartCachedObjectFactory(ObjectFactory<T> factory, CachedObjectFactoryTaskExecutor executor, int defaultObjectCount) {
        this(factory, 500, TimeUnit.MILLISECONDS, executor, defaultObjectCount);
    }

    public SmartCachedObjectFactory(ObjectFactory<T> factory, long monitorTime, TimeUnit timeUnit, CachedObjectFactoryTaskExecutor executor, int defaultObjectCount) {
        this.factory = factory;
        this.cache = new CopyOnWriteArrayList<>();
        suppliedObjects = new PreciseConcurrentTimeCountStatistics(timeUnit.toMillis(monitorTime));
        retrievedObjects = new PreciseConcurrentTimeCountStatistics(timeUnit.toMillis(monitorTime));
        createdObjects = new PreciseConcurrentTimeCountStatistics(timeUnit.toMillis(monitorTime));
        refill();
        task = new TaskImpl(timeUnit.toMillis(monitorTime));
        executor.submit(task);
        Cleaner.create(this, () -> executor.remove(task));
        this.defaultObjectCount = defaultObjectCount;
        this.maxObjectCount.set(defaultObjectCount);
    }

    @Override
    public T getInstance() {
        try {
            acquireLock();
            suppliedObjects.measure(1);
            if(cache.size() > 0) {
                T t = cache.remove(0);
                retrievedObjects.measure(1);
                return t;
            }
        } finally {
            lock.set(false);
        }

        createdObjects.measure(1);
        return factory.newInstance();
    }

    @Override
    public void putInstance(@Nullable T i) {
        if(i == null)
            return;
        i.recycle();
        try {
            acquirePutLock();
            if(cache.size() >= maxObjectCount.get())
                return;
            cache.add(i);
        } finally {
            putLock.set(false);
        }

    }

    @Override
    public void refill() {
        try {
            acquirePutLock();
            for(int i = 0; i < maxObjectCount.get() - cache.size(); i++) {
                cache.add(factory.newInstance());
            }
        } finally {
            putLock.set(false);
        }
    }

    @Override
    public int getMaxCachedObjectCount() {
        return maxObjectCount.get();
    }

    @Override
    public int getMinCachedObjectCount() {
        return defaultObjectCount;
    }

    @Override
    public int getCurrentCachedObjectCount() {
        try {
            acquirePutLock();
            return cache.size();
        } finally {
            putLock.set(false);
        }
    }

    private void acquireLock() {
        int attempts = 0;
        while(!lock.compareAndSet(false, true)) {
            sleep(exponentialBackoff(attempts));
            while(lock.get()) ;
        }
    }

    private void acquirePutLock() {
        int attempts = 0;
        while(!putLock.compareAndSet(false, true)) {
            sleep(exponentialBackoff(attempts));
            while(putLock.get()) ;
        }
    }

    private static final int MAX_EXPONENTIAL_BACKOFF_TIME = 500;

    private static int exponentialBackoff(int failedAttempts) {
        double v = (Math.pow(2, failedAttempts) - 1) / 2;
        return v > MAX_EXPONENTIAL_BACKOFF_TIME ? MAX_EXPONENTIAL_BACKOFF_TIME : (int) v;
    }

    private static void sleep(int ns) {
        long start = System.nanoTime();
        //noinspection StatementWithEmptyBody
        while(System.nanoTime() - start < ns) ;
    }

    private final class TaskImpl implements CachedObjectFactoryTaskExecutor.Task {
        private final long[][] data;
        @Getter
        private final long interval;

        public TaskImpl(long interval) {
            this.interval = interval;
            this.data = new long[5][4];
        }

        @Override
        public void execute() {
            try {
                acquireLock();
                newSample();
                long maxCreated = 0;
                long minDiff = Integer.MAX_VALUE;
                for(int i = 0; i < 5; i++) {
                    maxCreated = Math.max(MAX_CREATE_COUNT, data[i][3]);
                    long diff = (data[i][0] - data[i][1]) / 2;
                    if(diff > 1)
                        minDiff = Math.min(minDiff, diff);
                }

                if(maxCreated > 0) {
                    long min = Math.min(20, maxCreated / 2);
                    maxObjectCount.addAndGet((int) min);
                    for(int i = 0; i < min; i++) {//Do not create a lot of objects!
                        cache.add(factory.newInstance());
                    }
                } else if(minDiff != Integer.MAX_VALUE) {
                    for(int i = 0; i < minDiff; i++) {
                        cache.remove(0);
                    }
                    maxObjectCount.addAndGet((int) (-1 * minDiff));
                }
            } finally {
                lock.set(false);
            }
        }

        private void newSample() {
            System.arraycopy(data, 1, data, 0, 4);
            data[4][0] = cache.size();
            data[4][1] = suppliedObjects.getCount();
            data[4][2] = retrievedObjects.getCount();
            data[4][3] = createdObjects.getCount();
        }
    }
}
