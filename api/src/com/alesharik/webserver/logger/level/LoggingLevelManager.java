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

package com.alesharik.webserver.logger.level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * This is main interface in Logging Levels system
 *
 * @see LoggingLevel
 * @see Level
 * @see LoggingLevelManagerMXBean
 */
@ThreadSafe
public interface LoggingLevelManager extends LoggingLevelManagerMXBean {
    /**
     * Return logging level by name
     *
     * @param name logging level name
     * @return logging level or <code>null</code> if no logging level exists
     */
    @Nullable
    LoggingLevel getLoggingLevel(@Nonnull String name);

    /**
     * Create new enabled logging level
     *
     * @param name level name
     * @return new logging level if no one exists, or already created logging level
     */
    @Nonnull
    LoggingLevel createLoggingLevel(@Nonnull String name);
}
