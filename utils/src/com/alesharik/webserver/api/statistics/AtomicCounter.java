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

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class uses atomic long to provide {@link Counter} functions
 */
@ThreadSafe
public class AtomicCounter implements Counter {
    protected final AtomicLong counter = new AtomicLong();

    public AtomicCounter() {
        counter.set(0);
    }

    /**
     * Creates atomic counter
     * @param value initial counter value
     */
    public AtomicCounter(long value) {
        counter.set(value);
    }

    @Override
    public long get() {
        return counter.get();
    }

    @Override
    public void add(long delta) {
        counter.addAndGet(delta);
    }

    @Override
    public void add() {
        counter.incrementAndGet();
    }

    @Override
    public long reset() {
        return counter.getAndSet(0);
    }
}
