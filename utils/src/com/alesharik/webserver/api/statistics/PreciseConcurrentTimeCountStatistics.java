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

import sun.misc.Cleaner;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class uses timer thread to update values
 */
@ThreadSafe
public class PreciseConcurrentTimeCountStatistics implements TimeCountStatistics {
    private static final ScheduledExecutorService DEFAULT_TIMER = Executors.newSingleThreadScheduledExecutor((r) -> {
        Thread thread = new Thread(r);
        thread.setName("PreciseConcurrentTimeCountStatistics-DefaultTimer");
        thread.setDaemon(true);
        thread.setPriority(Thread.MAX_PRIORITY - 1);
        return thread;
    });

    private final long delay;

    private final AtomicLong counter;
    private final AtomicLong time;


    public PreciseConcurrentTimeCountStatistics(long delay) {
        this(delay, DEFAULT_TIMER);
    }

    /**
     * @param delay in milliseconds
     */
    public PreciseConcurrentTimeCountStatistics(long delay, ScheduledExecutorService timer) {
        this.delay = delay;
        this.counter = new AtomicLong();
        this.time = new AtomicLong();

        update();
        ScheduledFuture<?> future = timer.scheduleAtFixedRate(this::update, 0, delay, TimeUnit.MILLISECONDS);
        Cleaner.create(this, () -> future.cancel(false));
    }

    @Override
    public void measure(int count) {
        counter.addAndGet(count);
    }

    @Override
    public void update() {
        long currentTime = System.currentTimeMillis();
        long l = time.get();
        if(currentTime - l < delay)
            return;

        long cnt = counter.get();
        while(!time.compareAndSet(l, currentTime)) {
            currentTime = System.currentTimeMillis();
            l = time.get();
            if(currentTime - l < delay)
                return;
            cnt = counter.get();
        }
        counter.compareAndSet(cnt, 0);
    }

    @Override
    public void reset() {
        time.set(0);
        counter.set(0);
    }

    @Override
    public long get() {
        return counter.get();
    }

}
