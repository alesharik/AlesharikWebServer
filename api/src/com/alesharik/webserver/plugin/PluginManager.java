package com.alesharik.webserver.plugin;

import java.io.File;

/**
 * This class load plugins
 */
@Deprecated
public interface PluginManager {
    void setup(File file);

    /**
     * Load plugins from archives
     */
    void loadPlugins();

    void setupPlugins();

    void start();

    void shutdown();
}
