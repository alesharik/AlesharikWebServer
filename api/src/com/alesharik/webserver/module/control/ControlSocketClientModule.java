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

package com.alesharik.webserver.module.control;

import com.alesharik.webserver.module.control.messaging.ControlSocketClientConnection;
import com.alesharik.webserver.configuration.Module;

import javax.annotation.Nonnull;
import java.io.IOException;

public interface ControlSocketClientModule extends Module, ControlSocketClientModuleMXBean {
    /**
     * Return "control-socket-client" - name of module
     */
    @Nonnull
    @Override
    default String getName() {
        return "control-socket-client";
    }

    /**
     * Create new connection to server
     *
     * @param host          server host
     * @param port          server port
     * @param authenticator {@link com.alesharik.webserver.module.control.messaging.ControlSocketClientConnection.Authenticator}
     * @return requested connection with server
     * @throws IOException if anything happens
     */
    ControlSocketClientConnection newConnection(String host, int port, ControlSocketClientConnection.Authenticator authenticator) throws IOException;
}
