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

package com.alesharik.webserver.server.api;

/**
 * Check if WSApplication can be registered in {@link org.glassfish.grizzly.websockets.WebSocketEngine}
 */
@Deprecated
public abstract class WSChecker {
    /**
     * Return true if WebSocket can be registered
     */
    public abstract boolean enabled();

    public static final class Disabled extends WSChecker {

        @Override
        public boolean enabled() {
            return false;
        }
    }

    public static final class Enabled extends WSChecker {

        @Override
        public boolean enabled() {
            return true;
        }
    }
}
