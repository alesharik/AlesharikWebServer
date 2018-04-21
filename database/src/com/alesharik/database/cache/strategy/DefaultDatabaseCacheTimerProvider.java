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
import java.util.concurrent.ScheduledExecutorService;

/**
 * This class provides default executor for all caching strategies.
 * It is required to set <code>alesharikwebserver-database-cache-timer-provider</code> property to use this provider as default!
 */
public interface DefaultDatabaseCacheTimerProvider {
    @Nonnull
    ScheduledExecutorService getTimer();
}
