package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.control.websockets.control.WebSocketController;
import com.alesharik.webserver.control.websockets.dashboard.DashboardWebSocketApplication;

public class ControlAccessManagerBuilder {
    private DashboardWebSocketApplication dashboardWebSocketApplication;
    private WebSocketController webSocketController;

    public void setDashboardWebSocketApplication(DashboardWebSocketApplication dashboardWebSocketApplication) {
        this.dashboardWebSocketApplication = dashboardWebSocketApplication;
    }

    public void setWebSocketController(WebSocketController webSocketController) {
        this.webSocketController = webSocketController;
    }

    public ControlAccessManager build() {
        ControlAccessManager manager = new ControlAccessManager();
        manager.dashboardWebSocketApplication = dashboardWebSocketApplication;
        manager.webSocketController = webSocketController;
        return manager;
    }
}
