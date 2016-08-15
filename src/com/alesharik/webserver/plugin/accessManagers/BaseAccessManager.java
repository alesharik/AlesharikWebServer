package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.main.FileManager;

public final class BaseAccessManager {
    FileManager fileManager;

    public FileManager getFileManager() {
        return fileManager;
    }
}
