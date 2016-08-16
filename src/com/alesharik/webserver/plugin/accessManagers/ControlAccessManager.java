package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.api.server.control.ControlWebSocketPlugin;
import com.alesharik.webserver.api.server.control.ControlWebSocketWrapper;
import com.alesharik.webserver.api.server.dashboard.DashboardWebsocketPlugin;
import com.alesharik.webserver.api.server.dashboard.DashboardWebsocketWrapper;
import com.alesharik.webserver.control.websockets.control.WebSocketController;
import com.alesharik.webserver.control.websockets.dashboard.DashboardWebSocketApplication;

public final class ControlAccessManager {
    DashboardWebSocketApplication dashboardWebSocketApplication;
    WebSocketController webSocketController;

    ControlAccessManager() {
    }

    public DashboardWebsocketWrapper registerNewDashboardWebsocketPlugin(DashboardWebsocketPlugin plugin) {
        return dashboardWebSocketApplication.registerNewPlugin(plugin);
    }

    public ControlWebSocketWrapper registerNewControlWebsocketPlugin(ControlWebSocketPlugin plugin) {
        return webSocketController.registerPlugin(plugin);
    }
}
