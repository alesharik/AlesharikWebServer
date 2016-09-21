package com.alesharik.webserver.plugin.accessManagers;

import java.util.Objects;

/**
 * This class used for create {@link PluginAccessManager} with all access managers.
 */
public class UltimatePluginAccessManagerBuilder {
    private BaseAccessManager baseAccessManager;
    private ControlAccessManager controlAccessManager;
    private ServerAccessManager serverAccessManager;
    private MicroserviceAccessManager microserviceAccessManager;

    public UltimatePluginAccessManagerBuilder() {
    }

    public UltimatePluginAccessManagerBuilder setBaseAccessManager(BaseAccessManager baseAccessManager) {
        Objects.requireNonNull(baseAccessManager);
        this.baseAccessManager = baseAccessManager;
        return this;
    }

    public UltimatePluginAccessManagerBuilder setControlAccessManager(ControlAccessManager controlAccessManager) {
        Objects.requireNonNull(controlAccessManager);
        this.controlAccessManager = controlAccessManager;
        return this;
    }

    public UltimatePluginAccessManagerBuilder setServerAccessManager(ServerAccessManager serverAccessManager) {
        Objects.requireNonNull(serverAccessManager);
        this.serverAccessManager = serverAccessManager;
        return this;
    }

    public UltimatePluginAccessManagerBuilder setMicroserviceAccessManager(MicroserviceAccessManager microserviceAccessManager) {
        this.microserviceAccessManager = microserviceAccessManager;
        return this;
    }

    public PluginAccessManager build() {
        return new PluginAccessManager(baseAccessManager, controlAccessManager, serverAccessManager, microserviceAccessManager);
    }
}
