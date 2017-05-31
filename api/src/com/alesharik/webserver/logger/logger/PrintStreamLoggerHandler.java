package com.alesharik.webserver.logger.logger;

import org.glassfish.grizzly.utils.Charsets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public final class PrintStreamLoggerHandler extends LoggerHandler {
    private OutputStreamWriter writer;
    private Charset charset;

    /**
     * Default encoding is <code>UTF-8</code>
     */
    public PrintStreamLoggerHandler() {
        charset = Charsets.UTF8_CHARSET;
    }

    @Override
    public synchronized void setOutputStream(OutputStream out) throws SecurityException {
        if(charset == null)
            charset = Charsets.UTF8_CHARSET;

        if(this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException e) {
                reportError(null, e, ErrorManager.CLOSE_FAILURE);
            }
        }
        this.writer = new OutputStreamWriter(out, charset);
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

    @Override
    public boolean isLoggable(LogRecord record) {
        if(record == null)
            return false;

        int levelValue = getLevel().intValue();
        if(record.getLevel().intValue() < levelValue || levelValue == Level.OFF.intValue()) {
            return false;
        }
        Filter filter = getFilter();
        return filter == null || filter.isLoggable(record);
    }

    @Override
    public synchronized void setEncoding(String encoding) throws SecurityException, UnsupportedEncodingException {
        if(encoding == null)
            return;
        charset = Charset.forName(encoding);
    }

    @Override
    public String getEncoding() {
        return charset.toString();
    }
}
