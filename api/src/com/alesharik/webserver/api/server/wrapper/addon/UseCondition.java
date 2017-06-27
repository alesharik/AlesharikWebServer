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

package com.alesharik.webserver.api.server.wrapper.addon;

import com.alesharik.webserver.api.server.wrapper.NetworkListenerConfiguration;

/**
 * This class used by HttpServerModule for enable required {@link HttpServerAddOn}s
 */
public interface UseCondition {
    /**
     * Check if {@link HttpServerAddOn} can be used in current {@link NetworkListenerConfiguration}
     *
     * @param networkListener configuration
     * @return true if {@link HttpServerAddOn} can be used, overwise false
     */
    boolean allow(NetworkListenerConfiguration networkListener);

    final class Always implements UseCondition {

        @Override
        public boolean allow(NetworkListenerConfiguration n) {
            return true;
        }
    }

    final class Never implements UseCondition {

        @Override
        public boolean allow(NetworkListenerConfiguration n) {
            return false;
        }
    }
}
