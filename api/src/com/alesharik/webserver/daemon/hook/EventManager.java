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

package com.alesharik.webserver.daemon.hook;

import javax.annotation.Nonnull;

/**
 * Event manager fire events
 *
 * @see com.alesharik.webserver.daemon.annotation.Daemon
 */
public interface EventManager {
    /**
     * Fire event. Do nothing if event not exists
     *
     * @param name event name
     * @param args event arguments
     */
    void fireEvent(@Nonnull String name, Object... args);
}
