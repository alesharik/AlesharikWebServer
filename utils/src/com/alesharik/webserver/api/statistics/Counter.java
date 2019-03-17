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

/**
 * This interface represents a basic thread-safe counter with increment, add, get and reset methods
 */
@ThreadSafe
public interface Counter {
    /**
     * Return counter value
     * @return counter value
     */
    long get();

    /**
     * Increment the counter
     */
    default void add() {
        add(1);
    }

    /**
     * Add value to counter
     * @param delta the value. May be negative
     */
    void add(long delta);

    /**
     * Reset counter to 0
     * @return counter value just before reset
     */
    long reset();
}
