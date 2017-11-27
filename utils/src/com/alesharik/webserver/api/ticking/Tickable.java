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

/**
 * This class may tick! If class is mutable, you may need override {@link CacheComparator}.{@link #objectEquals(Object)} and {@link CacheComparator}.{@link #objectHashCode()} methods.
 * All ticking classes are cached. If your class must be non cached, return <code>object == this</code> in {@link CacheComparator}.{@link #objectEquals(Object)}
 */
@FunctionalInterface
public interface Tickable extends CacheComparator {
    /**
     * This function called in ANOTHER THREAD.
     * Do not recommended to do long work(get response form internet server, database or etc) if you are not using thread pool.
     * Main logger log all exceptions with <code>[TickingPool]</code> prefix
     */
    void tick() throws Exception;

    @Override
    default boolean objectEquals(Object other) {
        return this.equals(other);
    }

    @Override
    default int objectHashCode() {
        return this.hashCode();
    }
}
