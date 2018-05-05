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

package com.alesharik.database.cache;

import com.alesharik.database.cache.strategy.CachingStrategy;

import javax.annotation.Nonnull;

/**
 * Cacheable classes hold and manage cached values. For example cacheable table will hold cached rows, cacheable scheme - tables, etc
 */
public interface Cacheable {
    /**
     * Reset all cached values to <code>null</code>
     */
    void timeout();

    /**
     * Update cached values
     */
    void update();

    /**
     * Set caching strategy
     *
     * @param cachingStrategy the strategy. <code>null</code> - disable
     */
    void setStrategy(@Nonnull CachingStrategy cachingStrategy);
}
