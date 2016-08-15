package com.alesharik.webserver.logger.handlers;

import com.alesharik.webserver.logger.LoggerFormatter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

class ConsoleHandler extends java.util.logging.ConsoleHandler {
    protected OutputStreamWriter writer;

    public ConsoleHandler() {
        configure();
        setOutputStream(System.err);
    }

    private void configure() {
        setLevel(getLevelProperty());
        setFilter(getFilterProperty());
        setFormatter(getFormatterProperty());
        try {
            setEncoding(getEncodingProperty());
        } catch (Exception e) {
            try {
                setEncoding(null);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    /**
     * Override this to set handler level
     */
    protected Level getLevelProperty() {
        return Level.ALL;
    }

    /**
     * Override this to set handler filter
     */
    protected Filter getFilterProperty() {
        return null;
    }

    /**
     * verride this to set handler formatter
     */
    protected Formatter getFormatterProperty() {
        return new LoggerFormatter();
    }

    /**
     * Override this to set handler encoding
     */
    protected String getEncodingProperty() {
        return null;
    }

    @Override
    protected synchronized void setOutputStream(OutputStream out) throws SecurityException {
        if(this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException e) {
                reportError(null, e, ErrorManager.CLOSE_FAILURE);
            }
        }
        this.writer = new OutputStreamWriter(out);
        super.setOutputStream(out);
    }

    @Override
    public void publish(LogRecord record) {
        if(!isLoggable(record)) {
            return;
        }

        String msg = getFormatter().format(record);
        try {
            this.writer.write(msg);
        } catch (IOException e) {
            reportError(null, e, ErrorManager.WRITE_FAILURE);
        } finally {
            flush();
        }
    }

    @Override
    public synchronized void flush() {
        try {
            this.writer.flush();
        } catch (IOException e) {
            reportError(null, e, ErrorManager.FLUSH_FAILURE);
        }
    }


}
