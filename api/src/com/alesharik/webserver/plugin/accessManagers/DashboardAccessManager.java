package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.control.dashboard.DashboardDataHolder;

public interface DashboardAccessManager {
    DashboardDataHolder getDataHolder();
}
