package com.alesharik.webserver.plugin.accessManagers;

import org.apache.commons.lang3.ArrayUtils;

public final class PluginAccessManager {
    private final BaseAccessManager baseAccessManager;
    private final ControlAccessManager controlAccessManager;
    private final ServerAccessManager serverAccessManager;

    public PluginAccessManager(BaseAccessManager baseAccessManager, ControlAccessManager controlAccessManager, ServerAccessManager serverAccessManager) {
        this.baseAccessManager = baseAccessManager;
        this.controlAccessManager = controlAccessManager;
        this.serverAccessManager = serverAccessManager;
    }

    public PluginAccessManager fromTemplate(String template) {
        BaseAccessManager baseAccessManager = null;
        ControlAccessManager controlAccessManager = null;
        ServerAccessManager serverAccessManager = null;
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
        return new PluginAccessManager(baseAccessManager, controlAccessManager, serverAccessManager);
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

    public enum AccessTypes {
        BASE("base"),
        SERVER("server"),
        CONTROL("control");

        private final String name;

        AccessTypes(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
