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

package com.alesharik.webserver.api.control.messaging;

import net.jcip.annotations.NotThreadSafe;

import java.io.IOException;

/**
 * This class is base ControlSocket connection
 */
@NotThreadSafe
public interface ControlSocketConnection {
    /**
     * Return remote host
     */
    String getRemoteHost();

    /**
     * Return remote port
     */
    int getRemotePort();

    /**
     * Send message to opponent
     *
     * @param message the message
     * @throws IOException if anything happens
     */
    void sendMessage(ControlSocketMessage message) throws IOException;
}
