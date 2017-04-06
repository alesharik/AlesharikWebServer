package com.alesharik.webserver.control.dashboard.websocket.plugins;

import com.alesharik.webserver.control.dashboard.websocket.DashboardWebSocketPlugin;
import com.alesharik.webserver.control.dashboard.websocket.WebSocketSender;

//TODO
public class MenuPluginsDashboardWebSocketPlugin extends DashboardWebSocketPlugin {
    public MenuPluginsDashboardWebSocketPlugin(WebSocketSender sender) {
        super(sender);
    }

    @Override
    public String getName() {
        return "menuPlugins";
    }
}
