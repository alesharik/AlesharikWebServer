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
 * Basic average counter implementation
 */
@ThreadSafe
public class BasicAverageCounter implements AverageCounter {
    protected final StampedLock lock = new StampedLock();
    protected final AtomicLong period;

    protected long lastTime;
    protected long lastAverage;
    protected long currentCount;
    protected long currentSum;

    /**
     * Create counter with default time period - 1 second
     */
    public BasicAverageCounter() {
        this(1, TimeUnit.SECONDS);
    }

    /**
     * Create average counter with custom time period
     *
     * @param time the period
     * @param unit time unit
     */
    public BasicAverageCounter(long time, @Nonnull TimeUnit unit) {
        period = new AtomicLong(unit.toMillis(time));
        lastTime = System.currentTimeMillis();
    }

    @Override
    public void setTimePeriod(long time, TimeUnit unit) {
        period.set(unit.toMillis(time));
    }

    @Override
    public long getAverage() {
        long ret;
        long stamp = lock.tryOptimisticRead();
        ret = lastAverage;
        if(!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                ret = lastAverage;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return ret;
    }

    @Override
    public void addUnit(long l) {
        long stamp = lock.writeLock();
        try {
            long d = System.currentTimeMillis() - lastTime;
            if(d >= period.get()) {
                lastTime = System.currentTimeMillis();
                lastAverage = currentCount == 0 ? 0 : (currentSum / currentCount);
                currentSum = 0;
                currentCount = 0;
            }
            currentSum += l;
            currentCount++;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public void reset() {
        long stamp = lock.writeLock();
        try {
            lastAverage = 0;
            currentCount = 0;
            currentSum = 0;
            lastTime = System.currentTimeMillis();
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public void update() {
        long stamp = lock.writeLock();
        try {
            lastTime = System.currentTimeMillis();
            lastAverage = currentCount == 0 ? 0 : (currentSum / currentCount);
            currentSum = 0;
            currentCount = 0;
        } finally {
            lock.unlockWrite(stamp);
        }
    }
}
