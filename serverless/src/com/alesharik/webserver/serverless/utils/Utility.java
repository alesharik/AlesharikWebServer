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

package com.alesharik.webserver.serverless.utils;

import com.alesharik.webserver.serverless.ServerlessAgent;

import javax.annotation.Nonnull;

/**
 * Utilities extend {@link ServerlessAgent} capabilities
 */
public interface Utility {
    /**
     * Init the utility
     *
     * @param agent the agent
     */
    void init(@Nonnull ServerlessAgent agent);

    /**
     * Shutdown the utility
     */
    void shutdown();

    /**
     * Shutdown the utility immediately
     */
    void shutdownNow();
}
