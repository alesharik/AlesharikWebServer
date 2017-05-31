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
