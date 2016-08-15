package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.main.FileManager;

public class BaseAccessManagerBuilder {
    private FileManager fileManager;

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }
}
