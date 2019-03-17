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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.TimeUnit;

/**
 * This counter count average of given units by time period
 */
@ThreadSafe
public interface AverageCounter {
    /**
     * Set time period
     * @param time the period
     * @param unit time unit
     */
    void setTimePeriod(long time, @Nonnull TimeUnit unit);

    /**
     * Return counted average
     * @return counted average
     */
    long getAverage();

    /**
     * Add unit
     * @param l the unit. May be negative
     */
    void addUnit(long l);

    /**
     * Reset counter to 0
     */
    void reset();

    /**
     * Force-update average
     */
    void update();
}
