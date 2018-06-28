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

import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Module loaded by
 * Module must have no-args constructor!
 */
public interface Module {
    void parse(@Nullable Element configNode);

    void reload(@Nullable Element configNode);

    void start();

    void shutdown();

    void shutdownNow();

    @Nonnull
    String getName();

    /**
     * Return module main layer. If layer is <code>null</code>, module don't have layers
     */
    @Nullable
    Layer getMainLayer();

    /**
     * Return module {@link HookManager} or null, if module has no custom events
     */
    @Nullable
    default HookManager getHookManager() {
        return null;
    }
}
