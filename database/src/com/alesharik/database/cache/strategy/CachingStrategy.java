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

package com.alesharik.database.cache.strategy;

import javax.annotation.Nonnull;

/**
 * Caching strategy fires updates and deletes events for cached objects.
 * Caching strategies can't store objects and must use weak referencing
 */
public interface CachingStrategy {
    /**
     * Returns empty caching strategy
     */
    @Nonnull
    static CachingStrategy noCaching() {
        return NoCacheStrategy.INSTANCE;
    }

    /**
     * Return caching strategy that timeout items after some time(creation/inactivity)
     *
     * @return timeout strategy builder instance
     * @see TimeoutCachingStrategy
     */
    @Nonnull
    static TimeoutCachingStrategy.Builder timeout() {
        return new TimeoutCachingStrategyImpl.BuilderImpl();
    }

    static UpdateCachingStrategy.Builder delay() {
        return null; //FIXME
    }

    /**
     * Add cached objects event listener
     *
     * @param listener the listener
     */
    void addListener(@Nonnull Listener listener);

    /**
     * Remove the listener
     *
     * @param listener the listener
     */
    void removeListener(@Nonnull Listener listener);

    /**
     * Event must be fired when cached object is created
     *
     * @param o the object
     */
    void created(@Nonnull Object o);

    /**
     * Event must be fired on every write on a cached object
     *
     * @param o the cached object
     */
    void updated(@Nonnull Object o);

    /**
     * Event must be fired on every read from a cached object
     *
     * @param o the cached object
     */
    void activate(@Nonnull Object o);

    /**
     * Reset all scheduled tasks and internal state. This can block A LOT!
     */
    void reset();

    interface Listener {
        /**
         * Timeout(delete) object
         *
         * @param o the object
         */
        void timeout(@Nonnull Object o);

        /**
         * Sync object's contents with db(pull from db)
         *
         * @param o the object
         */
        void update(@Nonnull Object o);
    }
}
