package com.alesharik.webserver.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

//TODO rwrite this
public final class FileReciever {
    private File file;

    public FileReciever(File file) throws IOException {
        if(file.isDirectory()) {
            throw new FileNotFoundException();
        }
        if(!file.exists()) {
            if(!file.createNewFile()) {
                throw new IOException();
            }
        }
        this.file = file;
    }

    public void read(InputStream stream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for(int i = 0; i < Math.ceil(stream.available() / 1024F); i++) {
            stream.read(buffer);
            byteArrayOutputStream.write(buffer);
            buffer = new byte[1024];
        }
        Files.write(file.toPath(), byteArrayOutputStream.toByteArray());
    }
}
