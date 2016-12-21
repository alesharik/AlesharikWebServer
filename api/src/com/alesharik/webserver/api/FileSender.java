package com.alesharik.webserver.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public final class FileSender {
    private File file;

    public FileSender(File file) throws FileNotFoundException {
        if(!file.exists() || file.isDirectory()) {
            throw new FileNotFoundException();
        }

        this.file = file;
    }

    public void send(OutputStream out) throws IOException, OutOfMemoryError {
        byte[] bytes = Files.readAllBytes(file.toPath());
        byte[] buffer = new byte[1024];
        for(int i = 0; i < Math.ceil(bytes.length / 1024F); i++) {
            System.arraycopy(bytes, 1024 * i, buffer, 0, 1024);
            out.write(buffer);
            buffer = new byte[1024];
        }
        out.flush();
    }
}
