package com.alesharik.webserver.plugin.accessManagers;

import org.apache.commons.lang3.ArrayUtils;

public final class PluginAccessManager {
    private final BaseAccessManager baseAccessManager;
    private final ControlAccessManager controlAccessManager;
    private final ServerAccessManager serverAccessManager;
    private final MicroserviceAccessManager microserviceAccessManager;

    /**
     * Because i can't normally set final fields :(
     */
    PluginAccessManager(BaseAccessManager baseAccessManager, ControlAccessManager controlAccessManager,
                        ServerAccessManager serverAccessManager, MicroserviceAccessManager microserviceAccessManager) {
        this.baseAccessManager = baseAccessManager;
        this.controlAccessManager = controlAccessManager;
        this.serverAccessManager = serverAccessManager;
        this.microserviceAccessManager = microserviceAccessManager;
    }

    public PluginAccessManager fromTemplate(String template) {
        BaseAccessManager baseAccessManager = null;
        ControlAccessManager controlAccessManager = null;
        ServerAccessManager serverAccessManager = null;
        MicroserviceAccessManager microserviceAccessManager = null;
        String[] parts = template.replace(" ", "").split(",");
        if(ArrayUtils.contains(parts, AccessTypes.BASE)) {
            baseAccessManager = this.baseAccessManager;
        }
        if(ArrayUtils.contains(parts, AccessTypes.CONTROL)) {
            controlAccessManager = this.controlAccessManager;
        }
        if(ArrayUtils.contains(parts, AccessTypes.SERVER)) {
            serverAccessManager = this.serverAccessManager;
        }
        if(ArrayUtils.contains(parts, AccessTypes.MICROSERVICE)) {
            microserviceAccessManager = this.microserviceAccessManager;
        }
        return new PluginAccessManager(baseAccessManager, controlAccessManager, serverAccessManager, microserviceAccessManager);
    }

    public BaseAccessManager getBaseAccessManager() {
        return baseAccessManager;
    }

    public ControlAccessManager getControlAccessManager() {
        return controlAccessManager;
    }

    public ServerAccessManager getServerAccessManager() {
        return serverAccessManager;
    }

    public MicroserviceAccessManager getMicroserviceAccessManager() {
        return microserviceAccessManager;
    }

    private enum AccessTypes {
        BASE("base"),
        SERVER("server"),
        CONTROL("control"),
        MICROSERVICE("microservice");

        private final String name;

        AccessTypes(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
