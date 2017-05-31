package com.alesharik.webserver.logger.logger;

import java.io.OutputStream;
import java.util.logging.Handler;

public abstract class LoggerHandler extends Handler {
    @Override
    public void close() {
    }

    protected abstract void setOutputStream(OutputStream stream);
}
