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

package com.alesharik.webserver.configuration.config.ext;

import javax.annotation.Nonnull;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * This provider provides name binding for engine factory. It will be scanned in {@link com.alesharik.webserver.api.ExecutionStage#CORE_MODULES} only
 */
public interface ScriptEngineProvider {
    /**
     * Return script engine factory instance
     *
     * @return script engine factory instance
     */
    @Nonnull
    ScriptEngineFactory getEngineFactory();

    /**
     * Return main name of script factory
     *
     * @return main name of script factory
     */
    @Nonnull
    String getName();

    /**
     * Return misc helpers for current script engine
     *
     * @return misc helpers for current script engine
     */
    @Nonnull
    Helper getHelper();

    interface Helper {
        boolean hasFunction(String name, ScriptEngine engine);

        boolean hasFunction(String name, String code);

        Object executeFunction(String name, ScriptEngine engine);
    }
}
