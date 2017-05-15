package com.alesharik.webserver.logger;

import java.io.File;

/**
 * This exception means that logger not configured with {@link Logger#setupLogger(File, int)}
 */
public class LoggerNotConfiguredException extends RuntimeException {
    public LoggerNotConfiguredException() {
        super("Logger not configured!");
    }
}
