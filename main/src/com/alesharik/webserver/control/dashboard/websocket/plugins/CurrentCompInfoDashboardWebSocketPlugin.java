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
import java.util.Timer;
import java.util.TimerTask;

@Prefixes({"[DashboardWebSocketPlugin]", "[CurrentCompInfoDashboardWebSocketPlugin]"})
@Deprecated
public class CurrentCompInfoDashboardWebSocketPlugin extends DashboardWebSocketPlugin {
    private static final String SET_COMMAND = "set";
    private static final String START_COMMAND = "start";
    private static final String STOP_COMMAND = "stop";

    private final Timer timer;
    private Sender task;

    public CurrentCompInfoDashboardWebSocketPlugin(WebSocketSender sender) {
        super(sender);
        timer = new Timer("CurrentCompInfoDashboardWebSocketPlugin-SenderTimer", true);
        task = new Sender();
    }

    @Override
    public String getName() {
        return "currentCompInfo";
    }

    @Override
    public void receive(@Nonnull String command, @Nonnull String text) {
        switch (command) {
            case START_COMMAND:
                task = new Sender();
                timer.scheduleAtFixedRate(task, 0, 1000);
                break;
            case STOP_COMMAND:
                task.cancel();
                break;
            default:
                System.err.println("Unexpected command " + command + " with data " + text);
        }
    }

    @Override
    public void shutdownNow() {
        task.cancel();
    }

    @Override
    public void shutdown() {
        task.cancel();
    }

    private final class Sender extends TimerTask {
        @Override
        public void run() {
        }
    }
}
