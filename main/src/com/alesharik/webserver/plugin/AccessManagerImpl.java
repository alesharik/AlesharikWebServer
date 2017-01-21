package com.alesharik.webserver.plugin;

import com.alesharik.webserver.plugin.accessManagers.BaseAccessManager;
import com.alesharik.webserver.plugin.accessManagers.ControlAccessManager;
import com.alesharik.webserver.plugin.accessManagers.DashboardAccessManager;
import com.alesharik.webserver.plugin.accessManagers.MicroserviceAccessManager;
import com.alesharik.webserver.plugin.accessManagers.PluginAccessManager;
import com.alesharik.webserver.plugin.accessManagers.ServerAccessManager;
import lombok.Getter;

public final class AccessManagerImpl implements AccessManager {
    @Getter
    private BaseAccessManager baseAccessManager;

    @Getter
    private ControlAccessManager controlAccessManager;

    @Getter
    private DashboardAccessManager dashboardAccessManager;

    @Getter
    private MicroserviceAccessManager microserviceAccessManager;

    @Getter
    private PluginAccessManager pluginAccessManager;

    @Getter
    private ServerAccessManager serverAccessManager;

    @Getter
    private AccessPermissions[] permissions;

    AccessManagerImpl(AccessPermissions[] perms, BaseAccessManager baseAccessManager, ControlAccessManager controlAccessManager, DashboardAccessManager dashboardAccessManager, MicroserviceAccessManager microserviceAccessManager, PluginAccessManager pluginAccessManager, ServerAccessManager serverAccessManager) {
        this.baseAccessManager = baseAccessManager;
        this.controlAccessManager = controlAccessManager;
        this.dashboardAccessManager = dashboardAccessManager;
        this.microserviceAccessManager = microserviceAccessManager;
        this.pluginAccessManager = pluginAccessManager;
        this.serverAccessManager = serverAccessManager;
        this.permissions = perms;
    }
}
