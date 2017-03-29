package com.alesharik.webserver.plugin;

import java.io.File;
import java.util.concurrent.CopyOnWriteArraySet;

public final class PluginManagerImpl implements PluginManager {
    private final CopyOnWriteArraySet<File> pluginFiles;

    public PluginManagerImpl() {
        pluginFiles = new CopyOnWriteArraySet<>();
    }

    @Override
    public void setup(File file) {
        scan(file);
    }

    private void scan(File folder) {
        if(folder.isFile()) {
            if(folder.getName().endsWith(".jar")) {
                pluginFiles.add(folder);

            }
        } else {
            File[] files = folder.listFiles();
            if(files != null) {
                for(File file : files) {
                    scan(file);
                }
            }
        }
    }

    @Override
    public void loadPlugins() {

    }

    @Override
    public void setupPlugins() {

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
