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

package com.alesharik.webserver.control.dashboard.websocket;

import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dashboard Web Socket. Contains simply ping-like login protocol
 */
final class DashboardWebSocket extends DefaultWebSocket {
    private static final String HELLO_MSG = "hello";

    private final AtomicBoolean isLoggedIn;

    public DashboardWebSocket(ProtocolHandler protocolHandler, HttpRequestPacket request, WebSocketListener... listeners) {
        super(protocolHandler, request, listeners);
        isLoggedIn = new AtomicBoolean(false);
    }

    @Override
    public void onMessage(String text) {
        if(isLoggedIn.get()) {
            super.onMessage(text);
        } else if(HELLO_MSG.equals(text)) {
            isLoggedIn.set(true);
            send(HELLO_MSG);
        }
    }
}
