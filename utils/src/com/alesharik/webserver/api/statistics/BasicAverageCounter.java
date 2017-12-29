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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.StampedLock;

public class BasicAverageCounter implements AverageCounter {
    protected final StampedLock lock = new StampedLock();

    protected final AtomicLong delay = new AtomicLong(1000);

    protected volatile long lastTime = 0;
    protected volatile long lastAverage = 0;
    protected volatile long currentAvg = 0;

    @Override
    public void setTimeDelay(long time, TimeUnit unit) {
        delay.set(unit.toMillis(time));
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
            if(d >= delay.get()) {
                lastTime = System.currentTimeMillis();
                lastAverage = currentAvg;
                currentAvg = 0;
            }

            currentAvg = currentAvg == 0 ? l : (currentAvg + l) / 2;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public void reset() {
        long stamp = lock.writeLock();
        try {
            lastAverage = 0;
            currentAvg = 0;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public void update() {
        long stamp = lock.writeLock();
        try {
            lastTime = System.currentTimeMillis();
            lastAverage = currentAvg;
            currentAvg = 0;
        } finally {
            lock.unlockWrite(stamp);
        }
    }
}
