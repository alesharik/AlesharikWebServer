package com.alesharik.webserver.plugin;

import com.alesharik.webserver.plugin.accessManagers.BaseAccessManager;
import com.alesharik.webserver.plugin.accessManagers.ControlAccessManager;
import com.alesharik.webserver.plugin.accessManagers.ServerAccessManager;

public class PluginManagerBuilder {
    private BaseAccessManager baseAccessManager = null;
    private ControlAccessManager controlAccessManager = null;
    private ServerAccessManager serverAccessManager = null;

    public PluginManagerBuilder setBaseAccessManager(BaseAccessManager baseAccessManager) {
        if(baseAccessManager == null) {
            throw new IllegalArgumentException();
        }
        this.baseAccessManager = baseAccessManager;
        return this;
    }

    public PluginManagerBuilder setControlAccessManager(ControlAccessManager controlAccessManager) {
        if(controlAccessManager == null) {
            throw new IllegalArgumentException();
        }
        this.controlAccessManager = controlAccessManager;
        return this;
    }

    public PluginManagerBuilder setServerAccessManager(ServerAccessManager serverAccessManager) {
        if(serverAccessManager == null) {
            throw new IllegalArgumentException();
        }
        this.serverAccessManager = serverAccessManager;
        return this;
    }

    public PluginManager build() {
        if(baseAccessManager == null || controlAccessManager == null || serverAccessManager == null) {
            throw new IllegalArgumentException();
        }
        return new PluginManager(new PluginAccessManager(baseAccessManager, controlAccessManager, serverAccessManager));
    }
}
