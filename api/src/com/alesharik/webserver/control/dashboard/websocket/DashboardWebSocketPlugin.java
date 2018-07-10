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

import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * DashboardWebSocket plugin used for communicate with dashboard through WebSocket
 */
@Deprecated
public abstract class DashboardWebSocketPlugin extends DashboardWebSocketPluginSubModule {
    private final WebSocketSender sender;
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PACKAGE)
    private DashboardDataHolder dashboardDataHolder;

    public DashboardWebSocketPlugin(WebSocketSender sender) {
        this.sender = sender;
    }

    /**
     * On text message received
     */
    public void receive(@Nonnull String command, @Nonnull String text) {
    }

    /**
     * On <code>byte[]</code> message received
     */
    public void receive(@Nonnull String command, byte[] data) {
    }

    /**
     * Send command from this plugin to Dashboard
     *
     * @param command command
     * @param data    data for command
     */
    public final void send(String command, String data) {
        sender.send(getName(), command, data);
    }

    /**
     * Send command from this plugin to Dashboard
     *
     * @param command command
     * @param data    data for command.
     */
    public final void send(String command, byte[] data) {
        sender.send(getName(), command, data);
    }
}
