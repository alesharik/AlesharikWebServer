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

package com.alesharik.webserver.control.dashboard.websocket.plugins;

import com.alesharik.webserver.control.dashboard.websocket.DashboardWebSocketPlugin;
import com.alesharik.webserver.control.dashboard.websocket.WebSocketSender;
import com.alesharik.webserver.logger.Prefixes;

import javax.annotation.Nonnull;

/**
 * Used in navigator
 */
@Prefixes({"[DashboardWebSocketPlugin]", "[MenuDashboardWebSocketPlugin]"})
@Deprecated
public class MenuDashboardWebSocketPlugin extends DashboardWebSocketPlugin {
    public MenuDashboardWebSocketPlugin(WebSocketSender sender) {
        super(sender);
    }

    @Override
    public String getName() {
        return "menu";
    }

    @Override
    public void receive(@Nonnull String command, @Nonnull String text) {
        switch (command) {
            case "update":
                send("render", "");
                break;
            default:
                System.err.println("Unexpected command " + command + " with data " + text);
        }
    }
}
