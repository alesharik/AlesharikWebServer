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
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Timeout caching strategy timeouts objects after some time
 */
public interface TimeoutCachingStrategy extends CachingStrategy {
    /**
     * The timeout caching strategy builder
     */
    interface Builder {
        /**
         * Use specified timer
         *
         * @param executorService the timer
         * @return this
         */
        @Nonnull
        Builder withTimer(@Nonnull ScheduledExecutorService executorService);

        /**
         * Enable creation timeout. Object will be timed out after defined period of time since the moment of creation
         *
         * @param timeout the period, <code>null</code> - disable
         * @return this
         */
        @Nonnull
        Builder withCreationTimeout(@Nullable Duration timeout);

        /**
         * Enable update timeout. Object will be timed out after defined period of time since object's last update time
         *
         * @param timeout the period, <code>null</code> - disable
         * @return this
         */
        @Nonnull
        Builder withUpdateTimeout(@Nonnull Duration timeout);

        /**
         * Override update trigger. All updates will be perceived as activates
         *
         * @param perceive the flag, <code>true</code> - enable this feature, <code>false</code> - disable it
         * @return this
         */
        @Nonnull
        Builder perceiveUpdateAsActivate(boolean perceive);

        /**
         * Enable activation timeout. Object will be timed out after defined period of time since object's last activation time
         *
         * @param timeout the period, <code>null</code> - disable
         * @return this
         */
        @Nonnull
        Builder withActivateTimeout(@Nonnull Duration timeout);

        /**
         * Ignore reset method
         *
         * @return this
         */
        @Nonnull
        Builder ignoreReset();

        /**
         * Build the strategy
         *
         * @return the strategy
         */
        @Nonnull
        TimeoutCachingStrategy build();
    }
}
