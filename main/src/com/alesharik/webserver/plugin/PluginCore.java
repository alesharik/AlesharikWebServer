package com.alesharik.webserver.plugin;

import com.alesharik.webserver.plugin.accessManagers.PluginAccessManager;

//TODO write this
public abstract class PluginCore {
    protected PluginAccessManager pluginAccessManager;

    /**
     * Return it's real name. This name should equals Name parameter in meta file!
     */
    public abstract String getName();

    public abstract void run();

    public void setPluginAccessManager(PluginAccessManager manager) {
        pluginAccessManager = manager;
    }
}
