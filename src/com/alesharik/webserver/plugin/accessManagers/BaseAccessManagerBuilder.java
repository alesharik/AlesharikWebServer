package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.main.FileManager;

public class BaseAccessManagerBuilder {
    private FileManager fileManager;

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public BaseAccessManager build() {
        BaseAccessManager manager = new BaseAccessManager();
        manager.fileManager = fileManager;
        return manager;
    }
}
