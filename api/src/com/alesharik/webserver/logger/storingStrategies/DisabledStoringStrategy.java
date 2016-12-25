package com.alesharik.webserver.logger.storingStrategies;

import java.io.File;

public final class DisabledStoringStrategy extends StoringStrategy {
    public DisabledStoringStrategy(File file) {
        super(file);
    }

    @Override
    public void open() {

    }

    @Override
    public void publish(String prefix, String message) {

    }

    @Override
    public void close() {

    }
}
