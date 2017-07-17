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

public class BasicAverageCounter implements AverageCounter {
    protected final AtomicLong delay = new AtomicLong(1000);

    protected final AtomicLong lastTime = new AtomicLong(0);
    protected final AtomicLong lastAverage = new AtomicLong(0);
    protected final AtomicLong currentAvg = new AtomicLong(0);

    @Override
    public void setTimeDelay(long time, TimeUnit unit) {
        delay.set(unit.toMillis(time));
    }

    @Override
    public long getAverage() {
        return lastAverage.get();
    }

    @Override
    public void addUnit(long l) {
        long d = System.currentTimeMillis() - lastTime.get();
        if(d >= delay.get()) {
            lastTime.set(System.currentTimeMillis());
            lastAverage.set(currentAvg.getAndSet(0));
        }

        long lastAvg = currentAvg.get();
        long realAvg = (lastAvg + l) / 2;
        while(!currentAvg.compareAndSet(lastAvg, realAvg)) {
            lastAvg = currentAvg.get();
            realAvg = (lastAvg + l) / 2;
        }
    }

    @Override
    public void reset() {
        lastAverage.set(0);
        currentAvg.set(0);
    }
}
