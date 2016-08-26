package com.alesharik.webserver.plugin;

import com.alesharik.webserver.plugin.accessManagers.PluginAccessManager;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class used for holds {@link PluginHolder}
 */
class PluginHolderPool {
    private final PluginAccessManager mainAccessManager;
    private final CopyOnWriteArrayList<PluginHolder> holders = new CopyOnWriteArrayList<>();

    public PluginHolderPool(PluginAccessManager mainAccessManager) {
        this.mainAccessManager = mainAccessManager;
    }

    public synchronized void addPlugin(PluginCore core, MetaFile metaFile) {
        PluginHolder holder = new PluginHolder(core, mainAccessManager.fromTemplate(metaFile.getAttribute("Access")));
        holder.start();
        holders.add(holder);
    }

    public boolean isRunning(String name) {
        for(PluginHolder holder : holders) {
            if(holder.getName().equals(name)) {
                return holder.isAlive();
            }
        }
        return false;
    }
}
