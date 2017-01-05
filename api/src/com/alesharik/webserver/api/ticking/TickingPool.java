package com.alesharik.webserver.api.ticking;

import java.util.concurrent.TimeUnit;

/**
 * This interface used for implement ticking pool.
 * The ticking pool is {@link Tickable} pool, used for execute {@link Tickable}.tick() in right time
 * The {@link Tickable} tick in another thread
 *
 * @see Tickable
 */
public interface TickingPool {
    /**
     * This method add new tickable to pool. The element start ticking after it added to pool
     *
     * @param tickable the tickable to add
     * @param period   period before ticks
     * @param timeUnit used for convert period to milliseconds
     * @throws IllegalArgumentException if periodInMs <= 0
     * @throws NullPointerException     if tickable == <code>null</code>
     */
    default void startTicking(Tickable tickable, long period, TimeUnit timeUnit) {
        startTicking(tickable, timeUnit.toMillis(period));
    }

    /**
     * This method add new tickable to pool. The element start ticking after it added to pool
     *
     * @param tickable   the tickable to add
     * @param periodInMs period before ticks in milliseconds
     * @throws IllegalArgumentException if periodInMs <= 0
     * @throws NullPointerException     if tickable == <code>null</code>
     */
    void startTicking(Tickable tickable, long periodInMs);

    /**
     * This method stop {@link Tickable} and delete it from pool
     *
     * @param tickable the tickable to delete
     * @throws NullPointerException if tickable == <code>null</code>
     */
    void stopTicking(Tickable tickable);

    /**
     * Pause {@link Tickable} and NOT delete it
     *
     * @param tickable the tickable to pause
     * @throws NullPointerException if tickable == <code>null</code>
     */
    void pauseTickable(Tickable tickable);

    /**
     * Resume tickable from pause
     *
     * @param tickable the tickable to resume
     * @throws NullPointerException if tickable == <code>null</code>
     */
    void resumeTickable(Tickable tickable);

    /**
     * Return true if tickable is not sleeping and exists, otherwise false
     *
     * @param tickable the tickable
     * @throws NullPointerException if tickable == <code>null</code>
     */
    boolean isRunning(Tickable tickable);

    /**
     * Shutdown normally(can execute tasks, etc)
     */
    void shutdown();

    /**
     * Shutdown now(only stop threads and do cleanup work)
     */
    void shutdownNow();
}
