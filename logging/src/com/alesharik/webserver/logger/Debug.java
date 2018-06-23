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

package com.alesharik.webserver.logger;

import lombok.experimental.UtilityClass;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class provides debug logging
 */
@UtilityClass
public class Debug {
    public static final Logger.TextFormatter TEXT_FORMATTER = new Logger.TextFormatter(Logger.ForegroundColor.BLUE, Logger.BackgroundColor.NONE);
    public static final String DEBUG_PREFIX = TEXT_FORMATTER.format("[DEBUG]");
    private static final AtomicBoolean enabled = new AtomicBoolean(false);

    public static void enable() {
        enabled.set(true);
        for(String s : Logger.getLoggingLevelManager().getLoggingLevels())
            Logger.getLoggingLevelManager().getLoggingLevel(s).enable();
    }

    public static void disable() {
        enabled.set(false);
    }

    public static boolean isEnabled() {
        return enabled.get();
    }

    public static void log(String message) {
        if(isEnabled())
            Logger.logMessageUnsafeDebug(DEBUG_PREFIX, TEXT_FORMATTER.format(message), 2);
    }

    public static void log(String prefix, String message) {
        if(isEnabled())
            Logger.logMessageUnsafeDebug(DEBUG_PREFIX + TEXT_FORMATTER.format(prefix), TEXT_FORMATTER.format(message), 2);
    }

    public static void log(String message, String... prefixes) {
        if(isEnabled()) {
            String join = String.join("", prefixes);
            Logger.logMessageUnsafeDebug(DEBUG_PREFIX + (join.isEmpty() ? "" : TEXT_FORMATTER.format(join)), TEXT_FORMATTER.format(message), 2);
        }
    }

    public static void log(Throwable message) {
        if(isEnabled())
            Logger.logThrowableDebug(message, 2, TEXT_FORMATTER, DEBUG_PREFIX);
    }

    public static void log(Throwable message, String prefix) {
        if(isEnabled())
            Logger.logThrowableDebug(message, 2, TEXT_FORMATTER, DEBUG_PREFIX + prefix);
    }

    public static void log(Throwable message, String... prefixes) {
        if(isEnabled())
            Logger.logThrowableDebug(message, 2, TEXT_FORMATTER, DEBUG_PREFIX + String.join("", prefixes));
    }
}
