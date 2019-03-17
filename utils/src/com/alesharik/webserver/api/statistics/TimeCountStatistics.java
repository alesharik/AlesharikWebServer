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
 * This interface is used for collect a count in time period. In the next time period count will be reset
 */
@ThreadSafe
public interface TimeCountStatistics {
    /**
     * Add count to statistics value
     * @param count the count. May be negative
     */
    void measure(int count);

    /**
     * Force-update statistics
     */
    void update();

    /**
     * Reset count to 0
     */
    void reset();

    /**
     * Return count
     * @return the count
     */
    long get();
}
