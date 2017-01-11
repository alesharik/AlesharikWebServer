package com.alesharik.webserver.api.ticking;

/**
 * This is a MXBean for all {@link TickingPool}s
 */
public interface TickingPoolMXBean {
    /**
     * Return thread cont of current {@link TickingPool}
     */
    int getThreadCount();

    /**
     * Return total task count
     */
    int getTotalTaskCount();

    /**
     * Return running task count
     */
    int getRunningTaskCount();

    /**
     * Return pause task count
     */
    int getPauseTaskCount();
}
