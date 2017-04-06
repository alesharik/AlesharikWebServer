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
