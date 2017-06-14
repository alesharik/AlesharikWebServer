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

package com.alesharik.webserver.api.server.dashboard;

import com.alesharik.webserver.control.websockets.dashboard.DashboardWebSocketApplication;

/**
 * This class used for give access to websocket
 */
@Deprecated
public final class DashboardWebsocketWrapper {
    private final DashboardWebSocketApplication application;
    private final DashboardWebsocketPlugin plugin;

    public DashboardWebsocketWrapper(DashboardWebSocketApplication application, DashboardWebsocketPlugin plugin) {
        this.application = application;
        this.plugin = plugin;
    }

    public void sendMessage(String message) {
        application.sendMessage(plugin.getName(), message);
    }

    public String getName() {
        return plugin.getName();
    }

    public void onMessage(String message) {
        plugin.onMessage(message);
    }
}
