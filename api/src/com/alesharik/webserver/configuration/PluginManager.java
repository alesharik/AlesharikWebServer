package com.alesharik.webserver.configuration;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;

@ThreadSafe
public abstract class PluginManager extends Thread implements PluginManagerMXBean {
    protected final File workingFolder;
    protected final boolean hotReloadEnabled;

    public PluginManager(File workingFolder, boolean hotReloadEnabled) {
        this.workingFolder = workingFolder;
        this.hotReloadEnabled = hotReloadEnabled;
    }

    /**
     * Return true if manager finish all jobs
     */
    public abstract boolean isFree();
}
