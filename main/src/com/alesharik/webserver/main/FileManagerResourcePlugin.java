package com.alesharik.webserver.main;

public interface FileManagerResourcePlugin {
    String getAddress();

    byte[] getData();

    void setData(byte[] data);
}
