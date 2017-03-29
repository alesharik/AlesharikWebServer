package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.api.fileManager.FileManager;

@Deprecated
public interface BaseAccessManager {
    FileManager getFileManager();
}
