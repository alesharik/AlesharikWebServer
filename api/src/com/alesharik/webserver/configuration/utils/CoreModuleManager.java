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

package com.alesharik.webserver.configuration.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This manager manages all core modules
 */
public interface CoreModuleManager {
    /**
     * Return all core modules
     *
     * @return core modules list
     */
    @Nonnull
    List<CoreModule> getModules();

    /**
     * Return module by name
     *
     * @param name the name
     * @return the module or <code>null</code> if module not found
     */
    @Nullable
    CoreModule getModuleByName(@Nonnull String name);

    /**
     * Return classloader for core module
     *
     * @param module the core module
     * @return the classloader
     */
    @Nonnull
    CoreModuleClassLoader getClassLoader(@Nonnull CoreModule module);
}
