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

package com.alesharik.webserver.api;

import com.alesharik.webserver.base.mode.Mode;
import com.alesharik.webserver.base.mode.ModeClient;
import com.alesharik.webserver.base.mode.ModeGetter;
import com.alesharik.webserver.base.mode.ModeSwitch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class holds common {@link Gson} instance. It will always serialize nulls.
 * This class is in production mode by default.
 */
@UtilityClass
@ModeClient
public class GsonUtils {
    private static final Gson production;
    private static final Gson debug;
    private static final AtomicBoolean isDebug = new AtomicBoolean(false);

    static {
        production = new GsonBuilder()
                .serializeNulls()
                .create();
        debug = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .create();
    }

    /**
     * Return current {@link Gson} instance
     *
     * @return current {@link Gson} instance
     */
    public static Gson getGson() {
        return isDebug.get() ? debug : production;
    }

    /**
     * Return the debug {@link Gson }  instance with pretty-printing
     *
     * @return the debug {@link Gson} instance
     */
    public static Gson getDebugGson() {
        return debug;
    }

    /**
     * Return the production {@link Gson } instance with pretty-printing
     *
     * @return the production {@link Gson} instance
     */
    public static Gson getProductionGson() {
        return production;
    }

    /**
     * Enable debug mode
     */
    @ModeSwitch({Mode.DEBUG, Mode.DEVELOPMENT, Mode.TESTING, Mode.CI})
    public static void debugMode() {
        isDebug.set(true);
    }

    /**
     * Disable debug mode
     */
    @ModeSwitch({Mode.STAGING, Mode.PRODUCTION})
    public static void productionMode() {
        isDebug.set(false);
    }

    /**
     * Return true if this class is in debug mode
     *
     * @return true if this class is in debug mode
     */
    @ModeGetter(value = Mode.DEBUG, when = ModeGetter.GetterMode.TRUE)
    @ModeGetter(value = Mode.PRODUCTION, when = ModeGetter.GetterMode.FALSE)
    public static boolean isInDebug() {
        return isDebug.get();
    }
}
