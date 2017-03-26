package com.alesharik.webserver.logger.handlers;

import com.alesharik.webserver.logger.Logger;

import java.util.logging.Level;

/**
 * This class used for process Level.INFO
 */
public class InfoConsoleHandler extends SimpleConsoleHandler {

    public InfoConsoleHandler() {
        super();
        setOutputStream(Logger.getSystemOut());
    }

    @Override
    protected Level getLevelProperty() {
        return Level.INFO;
    }
}
