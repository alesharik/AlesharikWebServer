package com.alesharik.webserver.api.fileManager;

public interface FileManagerResourcePlugin {
    String getAddress();

    byte[] getData();

    void setData(byte[] data);
}
