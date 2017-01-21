package com.alesharik.webserver.plugin;

import com.alesharik.webserver.plugin.accessManagers.BaseAccessManager;
import com.alesharik.webserver.plugin.accessManagers.ControlAccessManager;
import com.alesharik.webserver.plugin.accessManagers.DashboardAccessManager;
import com.alesharik.webserver.plugin.accessManagers.MicroserviceAccessManager;
import com.alesharik.webserver.plugin.accessManagers.PluginAccessManager;
import com.alesharik.webserver.plugin.accessManagers.ServerAccessManager;

public interface AccessManager {
    BaseAccessManager getBaseAccessManager();

    ControlAccessManager getControlAccessManager();

    DashboardAccessManager getDashboardAccessManager();

    MicroserviceAccessManager getMicroserviceAccessManager();

    PluginAccessManager getPluginAccessManager();

    ServerAccessManager getServerAccessManager();

    AccessPermissions[] getPermissions();
}
