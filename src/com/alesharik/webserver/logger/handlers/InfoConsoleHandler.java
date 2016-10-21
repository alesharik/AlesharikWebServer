package com.alesharik.webserver.logger.handlers;

import java.util.logging.Level;

/**
 * This class used for process Level.INFO
 */
public class InfoConsoleHandler extends SimpleConsoleHandler {

    public InfoConsoleHandler() {
        super();
        setOutputStream(System.out);
    }

    @Override
    protected Level getLevelProperty() {
        return Level.INFO;
    }
}
