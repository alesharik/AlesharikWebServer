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

package com.alesharik.webserver.api.statistics;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.StampedLock;

/**
 * Basic implementation of {@link TimeCountStatistics}. It updates value on every {@link #update()}/{@link #measure(int)} method call
 */
@ThreadSafe
public class FuzzyTimeCountStatistics implements TimeCountStatistics {
    private final long delta;
    private final StampedLock lock = new StampedLock();
    private final AtomicLong count = new AtomicLong();

    private long time;
    private long lastCount;

    /**
     * Create statistics with time period
     * @param delta the period
     * @param timeUnit time unit
     */
    public FuzzyTimeCountStatistics(long delta, @Nonnull TimeUnit timeUnit) {
        this.delta = timeUnit.toMillis(delta);
        this.lastCount = 0;

        this.time = System.currentTimeMillis();
    }

    @Override
    public void measure(int count) {
        long current = System.currentTimeMillis();
        long l = lock.tryOptimisticRead();
        long time = this.time;
        if(!lock.validate(l)) {
            l = lock.readLock();
            try {
                time = this.time;
            } finally {
                lock.unlockRead(l);
            }
        }

        if(current < time + delta)
            this.count.addAndGet(count);
        else {
            long wl = lock.writeLock();
            try {
                lastCount = this.count.getAndSet(count);
                this.time = System.currentTimeMillis();
            } finally {
                lock.unlockWrite(wl);
            }
        }
    }

    @Override
    public void update() {
        long current = System.currentTimeMillis();
        long l = lock.tryOptimisticRead();
        long time = this.time;
        if(!lock.validate(l)) {
            l = lock.readLock();
            try {
                time = this.time;
            } finally {
                lock.unlockRead(l);
            }
        }

        if(current < time + delta)
            return;

        long wl = lock.writeLock();
        try {
            lastCount = this.count.getAndSet(0);
            this.time = System.currentTimeMillis();
        } finally {
            lock.unlockWrite(wl);
        }
    }

    @Override
    public void reset() {
        long l = lock.writeLock();
        try {
            lastCount = 0;
            count.set(0);
            time = System.currentTimeMillis();
        } finally {
            lock.unlockWrite(l);
        }
    }

    /**
     * This method is thread-safe
     */
    public long get() {
        long l = lock.tryOptimisticRead();
        long ret = lastCount;
        if(!lock.validate(l)) {
            l = lock.readLock();
            try {
                ret = lastCount;
            } finally {
                lock.unlockRead(l);
            }
        }
        return ret;
    }
}
