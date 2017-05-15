package com.alesharik.webserver.logger.storing;

import java.io.File;
import java.io.IOException;

public abstract class StoringStrategy {
    protected final File file;

    protected StoringStrategy(File file) {
        this.file = file;
    }

    public abstract void open() throws IOException;

    public abstract void publish(String prefix, String message);

    public abstract void close() throws IOException;

    protected void checkFile() throws IOException {
        if(!file.exists()) {
            if(!file.createNewFile()) {
                throw new IOException();
            }
        } else if(file.isDirectory()) {
            throw new IllegalArgumentException("Can't work with folder!");
        }
    }
}
