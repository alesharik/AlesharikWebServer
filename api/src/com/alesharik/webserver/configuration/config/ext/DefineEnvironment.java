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

/**
 * This class represent access point to parser's definitions
 */
public interface DefineEnvironment {
    /**
     * Get definition value for name
     *
     * @param name the name
     * @return definition or null if it isn't defined
     */
    @Nullable
    String getDefinition(@Nonnull String name);

    /**
     * Check if definition with specified name is defined
     *
     * @param name definition name
     * @return true if definition is defined, overwise false
     */
    boolean isDefined(String name);

    /**
     * Check if definition is provided by {@link DefineProvider}
     *
     * @param name definition name
     * @return true if definition is provided from {@link DefineProvider}, false - defined by user
     */
    boolean isProvided(String name);
}
