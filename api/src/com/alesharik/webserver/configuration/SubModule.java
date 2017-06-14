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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * SubModule is part of module. It is useful for split code into small independent pieces
 *
 * @implNote Implementation must be thread-safe
 */
@ThreadSafe
public interface SubModule {
    /**
     * Return submodule unique name
     */
    @Nonnull
    String getName();

    /**
     * Start submodule
     */
    void start();

    /**
     * Shutdown submodule without waiting
     */
    void shutdownNow();

    /**
     * Shutdown submodule gracefully
     */
    void shutdown();

    /**
     * Reload submodule(if config changed, etc) - shutdown and start it
     */
    default void reload() {
        shutdown();
        start();
    }

    /**
     * Return <code>true</code> if module started, overwise <code>false</code>
     */
    boolean isRunning();
}
