package com.alesharik.webserver.plugin;

import com.alesharik.webserver.plugin.accessManagers.BaseAccessManager;
import com.alesharik.webserver.plugin.accessManagers.ControlAccessManager;
import com.alesharik.webserver.plugin.accessManagers.MicroserviceAccessManager;
import com.alesharik.webserver.plugin.accessManagers.ServerAccessManager;
import com.alesharik.webserver.plugin.accessManagers.UltimatePluginAccessManagerBuilder;

/**
 * This class used for build {@link PluginManager}. Used only in AlesharikWebServer. DO NOT TRY USE IT!
 */
public class PluginManagerBuilder {
    private BaseAccessManager baseAccessManager = null;
    private ControlAccessManager controlAccessManager = null;
    private ServerAccessManager serverAccessManager = null;
    private MicroserviceAccessManager microserviceAccessManager = null;

    private boolean isMicroserviceServer = false;
    private boolean isRouterServer = false;

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

    public PluginManagerBuilder setMicroserviceAccessManager(MicroserviceAccessManager microserviceAccessManager) {
        if(microserviceAccessManager == null) {
            throw new IllegalArgumentException();
        }
        this.microserviceAccessManager = microserviceAccessManager;
        return this;
    }


    public PluginManagerBuilder isMicroserviceServer(boolean isMicroserviceServer) {
        this.isMicroserviceServer = isMicroserviceServer;
        return this;
    }

    public PluginManagerBuilder isRouterServer(boolean isRouterServer) {
        this.isRouterServer = isRouterServer;
        return this;
    }

    /**
     * @return PluginManager or null
     */
    public PluginManager build() {
        if(isRouterServer) {
            return null;
        }

        if(isMicroserviceServer && microserviceAccessManager == null) {
            throw new IllegalArgumentException();
        }
        if(baseAccessManager == null || controlAccessManager == null || serverAccessManager == null) {
            throw new IllegalArgumentException();
        }
        return new PluginManager(new UltimatePluginAccessManagerBuilder()
                .setBaseAccessManager(baseAccessManager)
                .setControlAccessManager(controlAccessManager)
                .setServerAccessManager(serverAccessManager)
                .setMicroserviceAccessManager(microserviceAccessManager)
                .build());
    }
}
