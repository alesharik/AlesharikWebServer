package com.alesharik.webserver.api.statistics;

/**
 * Used for collect count in time. Minimal value is millisecond.
 */
public interface TimeCountStatistics {
    void measure(int count);

    void update();

    long getCount();
}
