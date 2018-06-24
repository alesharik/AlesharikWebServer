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
import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

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

    /**
     * This helper wraps all interactions with script engine
     */
    interface Helper {
        /**
         * Check if current engine has function
         *
         * @param name   function name
         * @param engine the engine
         * @return <code>true</code> - engine contains the function, otherwise <code>false</code>
         */
        boolean hasFunction(@Nonnull String name, @Nonnull ScriptEngine engine);

        /**
         * Check if given script has function
         *
         * @param name function name
         * @param code the script
         * @return <code>true</code> - script contains function, otherwise <code>false</code>
         */
        boolean hasFunction(@Nonnull String name, @Nonnull String code);

        /**
         * Executes the function
         *
         * @param name   function name
         * @param engine the script engine
         * @return result of the function. If function returns nothing then <code>null</code> must be returned
         * @throws IllegalArgumentException if function not found
         * @throws ScriptExecutionError     if script error happened
         */
        @Nullable
        Object executeFunction(@Nonnull String name, @Nonnull ScriptEngine engine);

        /**
         * Executes given script
         *
         * @param script the script
         * @param engine the engine
         * @return result of the script. If script returns nothing then <code>null</code> must be returned
         * @throws ScriptExecutionError if script error happened
         */
        @Nullable
        default Object executeCode(@Nonnull String script, @Nonnull ScriptEngine engine) {
            try {
                return engine.eval(script);
            } catch (ScriptException e) {
                throw new ScriptExecutionError(e);
            }
        }
    }
}
