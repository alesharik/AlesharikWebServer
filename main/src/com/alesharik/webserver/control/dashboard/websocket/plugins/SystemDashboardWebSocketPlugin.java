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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nonnull;

@Prefixes({"[DashboardWebSocketPlugin]", "[SystemDashboardWebSocketPlugin]"})
public class SystemDashboardWebSocketPlugin extends DashboardWebSocketPlugin {
    private static final String GC_COMMAND = "gc";

    public SystemDashboardWebSocketPlugin(WebSocketSender sender) {
        super(sender);
    }

    @Override
    public String getName() {
        return "system";
    }

    @SuppressFBWarnings("DM_GC") //Because I need to call GC
    @Override
    public void receive(@Nonnull String command, @Nonnull String text) {
        switch (command) {
            case GC_COMMAND:
                System.gc();
                break;
            default:
                System.err.println("Command " + command + " with data " + text + " not expected!");
        }
    }
}
