package com.alesharik.webserver.plugin;

public abstract class PluginCore {
    protected final AccessManager baseAccessManager;

    public PluginCore(AccessManager accessManager) {
        this.baseAccessManager = accessManager;
    }

    public abstract void preInit();

    public abstract void init();

    public abstract void shutdown();

    /**
     * Return plugin unique name
     */
    public abstract String getName();
}
