package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.control.dashboard.PluginDataHolder;
import com.alesharik.webserver.main.FileManager;

public final class BaseAccessManager {
    FileManager fileManager;
    PluginDataHolder pluginDataHolder;

    public FileManager getFileManager() {
        return fileManager;
    }

    public PluginDataHolder getPluginDataHolder() {
        return pluginDataHolder;
    }
}
