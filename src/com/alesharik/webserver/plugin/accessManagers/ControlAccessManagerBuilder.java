package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.control.websockets.dashboard.DashboardWebSocketApplication;

public class ControlAccessManagerBuilder {
    private DashboardWebSocketApplication dashboardWebSocketApplication;

    public void setDashboardWebSocketApplication(DashboardWebSocketApplication dashboardWebSocketApplication) {
        this.dashboardWebSocketApplication = dashboardWebSocketApplication;
    }
}
