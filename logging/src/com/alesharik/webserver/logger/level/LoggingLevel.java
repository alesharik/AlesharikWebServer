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

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.NamedLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Logging levels represent flexible logging system. Each logging level can be enabled/disabled separately. It also can have
 * own {@link NamedLogger} and {@link com.alesharik.webserver.logger.Logger.TextFormatter}. Logging levels can be accessed directly
 * or with {@link Level} annotation. Logging levels also support {@link com.alesharik.webserver.logger.Prefixes} system.
 * Each Logging Level has it's unique name, which will be used as ID and message prefix
 *
 * @see LoggingLevelManager
 * @see Level
 */
@ThreadSafe
public interface LoggingLevel {
    /**
     * Enable logging level
     */
    void enable();

    /**
     * Disable logging level
     */
    void disable();

    /**
     * Return true if it is enabled, overwise false
     */
    boolean isEnabled();

    /**
     * Log simple message
     *
     * @param s the message
     */
    void log(@Nonnull String s);

    /**
     * Log exception
     *
     * @param e the exception
     */
    void log(@Nonnull Exception e);

    /**
     * Return logging level's unique name
     */
    @Nonnull
    String getName();

    /**
     * Bind {@link NamedLogger} to logging level
     *
     * @param namedLogger named logger or null
     */
    void setLogger(@Nullable NamedLogger namedLogger);

    /**
     * Returns current {@link NamedLogger} or <code>null</code> if this level doesn't have any {@link NamedLogger} already bound
     */
    @Nullable
    NamedLogger getLogger();

    /**
     * Not supported with {@link NamedLogger}. Set message {@link com.alesharik.webserver.logger.Logger.TextFormatter}
     *
     * @param loggerFormatter text formatter opr null
     */
    void setFormatter(@Nullable Logger.TextFormatter loggerFormatter);

    /**
     * Returns current {@link com.alesharik.webserver.logger.Logger.TextFormatter} or <code>null</code> if this level doesn't have any {@link com.alesharik.webserver.logger.Logger.TextFormatter} already set
     */
    @Nullable
    Logger.TextFormatter getFormatter();
}
