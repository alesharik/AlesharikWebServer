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

package com.alesharik.webserver.api.ticking;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * This interface used for implement ticking pool.
 * The ticking pool is {@link Tickable} pool, used for execute {@link Tickable}.tick() in right time
 * The {@link Tickable} tick in another thread
 *
 * @see Tickable
 */
public interface TickingPool extends TickingPoolMXBean {
    /**
     * This method add new tickable to pool. The element setup ticking after it added to pool
     *
     * @param tickable the tickable to add
     * @param period   period before ticks
     * @param timeUnit used for convert period to milliseconds
     * @throws IllegalArgumentException if periodInMs <= 0
     * @throws NullPointerException     if tickable == <code>null</code>
     */
    default void startTicking(@Nonnull Tickable tickable, long period, @Nonnull TimeUnit timeUnit) {
        startTicking(tickable, timeUnit.toMillis(period));
    }

    /**
     * This method add new tickable to pool. The element setup ticking after it added to pool
     *
     * @param tickable   the tickable to add
     * @param periodInMs period before ticks in milliseconds
     * @throws IllegalArgumentException if periodInMs <= 0
     * @throws NullPointerException     if tickable == <code>null</code>
     */
    void startTicking(@Nonnull Tickable tickable, long periodInMs);

    /**
     * This method shutdown {@link Tickable} and delete it from pool
     *
     * @param tickable the tickable to delete
     * @throws NullPointerException if tickable == <code>null</code>
     */
    void stopTicking(@Nonnull Tickable tickable);

    /**
     * Pause {@link Tickable} and NOT delete it
     *
     * @param tickable the tickable to pause
     * @throws NullPointerException if tickable == <code>null</code>
     */
    void pauseTickable(@Nonnull Tickable tickable);

    /**
     * Resume tickable from pause
     *
     * @param tickable the tickable to resume
     * @throws NullPointerException if tickable == <code>null</code>
     */
    void resumeTickable(@Nonnull Tickable tickable);

    /**
     * Return true if tickable is not sleeping and exists, otherwise false
     *
     * @param tickable the tickable
     * @throws NullPointerException if tickable == <code>null</code>
     */
    boolean isRunning(@Nonnull Tickable tickable);

    /**
     * Shutdown normally(can execute tasks, etc)
     */
    void shutdown();

    /**
     * Shutdown now(only shutdown threads and do cleanup work)
     */
    void shutdownNow();
}
