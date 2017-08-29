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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PreciseConcurrentTimeCountStatistics implements TimeCountStatistics {
    private static final Timer DEFAULT_TIMER = new Timer("PreciseConcurrentTimeCountStatistics-DefaultTimer", true);

    private final long delay;

    private final AtomicLong counter;
    private final AtomicLong time;

    private final AtomicInteger readerCount = new AtomicInteger();
    private final AtomicBoolean updateInProgress = new AtomicBoolean();


    public PreciseConcurrentTimeCountStatistics(long delay) {
        this(delay, DEFAULT_TIMER);
    }

    /**
     * @param delay in milliseconds
     */
    public PreciseConcurrentTimeCountStatistics(long delay, Timer timer) {
        this.delay = delay;
        this.counter = new AtomicLong();
        this.time = new AtomicLong();

        TimerTask timerTask = new TimerTaskImpl();
        //noinspection StatementWithEmptyBody
        while((System.nanoTime() % 1000000) < 900000) ;
        timer.scheduleAtFixedRate(timerTask, 0, delay);
    }

    @Override
    public void measure(int count) {
        //noinspection StatementWithEmptyBody
        while(updateInProgress.get()) ;

        readerCount.incrementAndGet();
        long last = counter.get();
        long calc = last + count;
        while(!counter.compareAndSet(last, calc)) {
            last = counter.get();
            calc = last + count;
        }
        readerCount.decrementAndGet();
    }

    @Override
    public void update() {
        long currentTime = System.currentTimeMillis();
        //noinspection StatementWithEmptyBody
        while(currentTime - time.get() < delay)
            currentTime = System.currentTimeMillis();

        updateInProgress.set(true);
        //Wait for threads
        //noinspection StatementWithEmptyBody
        while(readerCount.get() > 0) ;
        counter.set(0);
        time.set(currentTime);
        updateInProgress.set(false);
    }

    @Override
    public long getCount() {
        return counter.get();
    }

    private final class TimerTaskImpl extends TimerTask {

        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            //noinspection StatementWithEmptyBody
            while(currentTime - time.get() < delay)
                currentTime = System.currentTimeMillis();

            updateInProgress.set(true);
            //Wait for threads
            //noinspection StatementWithEmptyBody
            while(readerCount.get() > 0) ;
            counter.set(0);
            time.set(currentTime);
            updateInProgress.set(false);
        }
    }
}
