package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import com.alesharik.webserver.main.FileManager;

public class BaseAccessManagerBuilder {
    private FileManager fileManager;
    private DashboardDataHolder dashboardDataHolder;

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setDashboardDataHolder(DashboardDataHolder dashboardDataHolder) {
        this.dashboardDataHolder = dashboardDataHolder;
    }

    public BaseAccessManager build() {
        BaseAccessManager manager = new BaseAccessManager();
        manager.fileManager = fileManager;
        manager.dashboardDataHolder = dashboardDataHolder;
        return manager;
    }
}
