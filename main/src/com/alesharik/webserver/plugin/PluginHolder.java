package com.alesharik.webserver.plugin;

import com.alesharik.webserver.plugin.accessManagers.PluginAccessManager;

class PluginHolder extends Thread {
    private final PluginCore core;
    private final PluginAccessManager accessManager;

    public PluginHolder(PluginCore core, PluginAccessManager accessManager) {
        this.core = core;
        this.accessManager = accessManager;
        this.setDaemon(true);
        this.setName(core.getName());
    }

    public void run() {
        core.setPluginAccessManager(accessManager);
        core.run();
    }
}
