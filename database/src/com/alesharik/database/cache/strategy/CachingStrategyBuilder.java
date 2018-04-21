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

import lombok.experimental.UtilityClass;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * This class provides default executor service for DB
 */
@UtilityClass
final class CachingStrategyBuilder {
    static final ScheduledExecutorService EXECUTOR_SERVICE = get();

    static ScheduledExecutorService get() {
        String provider = System.getProperty("alesharikwebserver-database-cache-timer-provider");
        if(provider == null || provider.isEmpty())
            return Executors.newSingleThreadScheduledExecutor();
        else {
            try {
                Class<?> clazz = Class.forName(provider);
                DefaultDatabaseCacheTimerProvider prov = (DefaultDatabaseCacheTimerProvider) clazz.newInstance();
                return prov.getTimer();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                throw new Error(e);
            }
        }
    }
}
