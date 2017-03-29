package com.alesharik.webserver.configuration;

import java.io.File;
import java.util.Map;

public interface PluginManagerMXBean {

    /**
     * Return count of loaded files
     */
    int getLoadedFileCount();

    /**
     * Return true if PluginManager automatically reloads changed plugins
     */
    boolean isHotReloadEnabled();

    Map<File, ClassLoader> getClassLoaders();

    ThreadGroup getPluginThreadGroup();
}
