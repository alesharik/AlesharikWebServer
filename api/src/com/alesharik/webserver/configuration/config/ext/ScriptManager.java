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
import javax.script.ScriptEngineFactory;
import java.util.Set;

/**
 * Can be used by modules to crete script engines. You can get it from module context
 */
public interface ScriptManager {
    /**
     * Return engine factory by language name
     *
     * @param name language name
     * @return the engine factory. If Language not found then <code>null</code> will be returned
     */
    @Nullable
    ScriptEngineFactory getEngineFactory(@Nonnull String name);

    /**
     * Checks if language has support as script
     *
     * @param name language name
     * @return <code>true</code> - language has support, otherwise <code>false</code>
     */
    boolean hasLanguage(@Nonnull String name);

    /**
     * Return all available script languages
     *
     * @return the script language set
     */
    @Nonnull
    Set<String> getLanguages();

    /**
     * Return helper for language
     *
     * @param name language name
     * @return helper. If language not found, <code>null</code> will be returned
     */
    @Nullable
    ScriptEngineProvider.Helper getHelper(@Nonnull String name);
}
