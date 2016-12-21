package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.control.dashboard.PluginDataHolder;
import com.alesharik.webserver.main.FileManager;

public class BaseAccessManagerBuilder {
    private FileManager fileManager;
    private PluginDataHolder pluginDataHolder;

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setPluginDataHolder(PluginDataHolder pluginDataHolder) {
        this.pluginDataHolder = pluginDataHolder;
    }

    public BaseAccessManager build() {
        BaseAccessManager manager = new BaseAccessManager();
        manager.fileManager = fileManager;
        manager.pluginDataHolder = pluginDataHolder;
        return manager;
    }
}
