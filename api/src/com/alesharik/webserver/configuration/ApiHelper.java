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

package com.alesharik.webserver.configuration;

import com.alesharik.webserver.base.mode.Mode;
import com.alesharik.webserver.logger.Debug;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.logger.level.LoggingLevel;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

/**
 * Starts and shutdowns all API stuff
 */
@UtilityClass
@Prefixes({"[API]", "[INIT]"})
public class ApiHelper {
    public static void setupLogger(int listenerQueueCapacity, @Nonnull File file, boolean debug) throws IOException {
        if(!file.exists()) {
            if(!file.createNewFile())
                throw new IOException("Can't create new file! File: " + file.getAbsolutePath());
        } else {
            if(file.isDirectory())
                throw new IOException("File " + file.getAbsolutePath() + " is a directory!");
            if(!file.canRead())
                throw new IOException("Can't read from file " + file.getAbsolutePath() + " !");
        }

        Logger.setupLogger(file, listenerQueueCapacity);
        if(debug)
            Debug.enable();
    }

    public static void enableLoggingLevelOutput(@Nonnull String name) {
        LoggingLevel loggingLevel = Logger.getLoggingLevelManager().getLoggingLevel(name);
        if(loggingLevel != null)
            loggingLevel.enable();
    }

    public static void shutdownLogger() {
        Logger.shutdown();
    }

    public static void setMode(Mode mode) {
        //fixme
    }
}
