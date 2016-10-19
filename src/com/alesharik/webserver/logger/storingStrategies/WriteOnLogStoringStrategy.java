package com.alesharik.webserver.logger.storingStrategies;

import com.alesharik.webserver.logger.Logger;
import org.glassfish.grizzly.utils.Charsets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class WriteOnLogStoringStrategy extends StoringStrategy {
    private BufferedWriter writer;

    protected WriteOnLogStoringStrategy(File file) {
        super(file);
    }

    @Override
    public void open() throws IOException {
        checkFile();
        writer = Files.newBufferedWriter(file.toPath(), Charsets.UTF8_CHARSET);
    }

    @Override
    public void publish(String prefix, String message) {
        try {
            writer.write(prefix + ": " + message);
            writer.flush();
        } catch (IOException e) {
            Logger.log(e);
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
