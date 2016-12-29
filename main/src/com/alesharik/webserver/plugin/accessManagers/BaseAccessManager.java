package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import com.alesharik.webserver.main.FileManager;

public final class BaseAccessManager {
    FileManager fileManager;
    DashboardDataHolder dashboardDataHolder;

    public FileManager getFileManager() {
        return fileManager;
    }

    public DashboardDataHolder getDashboardDataHolder() {
        return dashboardDataHolder;
    }
}
