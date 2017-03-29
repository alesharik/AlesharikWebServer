package com.alesharik.webserver.plugin;

import java.util.concurrent.CopyOnWriteArrayList;

@Deprecated
class PluginHolderPool {
    private final CopyOnWriteArrayList<PluginCore> cores = new CopyOnWriteArrayList<>();

    public PluginHolderPool() {
    }

    public void addPlugin(PluginCore pluginCore) {
        cores.add(pluginCore);
        pluginCore.preInit();
        new Thread(pluginCore::init).start();
    }
}
