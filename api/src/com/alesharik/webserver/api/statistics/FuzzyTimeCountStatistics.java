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
import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.TimeUnit;

@NotThreadSafe
public class FuzzyTimeCountStatistics implements TimeCountStatistics {
    private final long delta;

    private volatile long count;
    private volatile long time;

    private volatile long lastCount;

    public FuzzyTimeCountStatistics(long delta, @Nonnull TimeUnit timeUnit) {
        this.delta = timeUnit.toMillis(delta);
        this.lastCount = 0;
        this.count = 0;

        this.time = System.currentTimeMillis();
    }

    public void measure(int count) {
        long current = System.currentTimeMillis();
        if(current < time + delta) {
            this.count += count;
        } else {
            lastCount = this.count;
            time = System.currentTimeMillis();
            this.count = count;
        }
    }

    public void update() {
        long current = System.currentTimeMillis();
        if(current >= time + delta) {
            lastCount = this.count;
            time = System.currentTimeMillis();
            count = 0;
        }
    }

    /**
     * This method is thread-safe
     */
    public long getCount() {
        return lastCount;
    }
}
