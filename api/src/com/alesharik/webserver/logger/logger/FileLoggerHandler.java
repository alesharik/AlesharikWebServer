package com.alesharik.webserver.logger.logger;

import lombok.SneakyThrows;
import org.glassfish.grizzly.utils.Charsets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * This console handler log every record into file. Use {@link FileOutputStream} for write to file
 */
public final class FileLoggerHandler extends LoggerHandler {
    private FileOutputStream outputStream;
    private AtomicReference<Charset> charset = new AtomicReference<>(Charsets.UTF8_CHARSET);

    /**
     * Create new FileConsoleHandler. Use {@link Level#ALL} by default
     *
     * @param file    file to write records. If file not exists, it will automatically create new one
     * @param charset file charset
     * @throws IllegalArgumentException if file is folder, can't write to file or can't create new file
     */
    @SneakyThrows
    public FileLoggerHandler(File file, Charset charset) {
        if(file.isDirectory())
            throw new IllegalArgumentException("File cannot be a directory!");
        else if(!file.exists())
            if(!file.createNewFile())
                throw new IllegalArgumentException("Cannot create new file!");
            else if(!file.canWrite())
                throw new IllegalArgumentException("Cannot write to file!");
        this.outputStream = new FileOutputStream(file);
        this.charset.set(charset);
        setLevel(Level.ALL);
    }

    /**
     * Create new FileConsoleHandler with <code>UTF-8</code> charset. Use {@link Level#ALL} by default
     *
     * @param file file to write records. If file not exists, it will automatically create new one
     * @throws IllegalArgumentException if file is folder, can't write to file or can't create new file
     */
    public FileLoggerHandler(File file) {
        this(file, Charsets.UTF8_CHARSET);
    }

    @Override
    public void publish(LogRecord record) {
        if(!isLoggable(record))
            return;
        String msg = getFormatter().format(record);
        try {
            outputStream.write(msg.getBytes(charset.get()));
        } catch (IOException e) {
            reportError(null, e, ErrorManager.WRITE_FAILURE);
        } finally {
            flush();
        }
    }

    @Override
    public synchronized void setEncoding(String encoding) throws SecurityException, UnsupportedEncodingException {
        Charset charset = Charset.forName(encoding);
        this.charset.set(charset);
    }

    @Override
    public synchronized String getEncoding() {
        if(this.charset == null)
            this.charset = new AtomicReference<>(Charsets.UTF8_CHARSET);
        if(this.charset.get() == null)
            this.charset.set(Charsets.UTF8_CHARSET);
        return this.charset.get().toString();
    }

    @Override
    public synchronized void flush() {
        try {
            outputStream.flush();
        } catch (IOException e) {
            reportError(null, e, ErrorManager.FLUSH_FAILURE);
        }
    }

    @Override
    public void close() {
        try {
            outputStream.close();
        } catch (IOException e) {
            reportError(null, e, ErrorManager.CLOSE_FAILURE);
        }
    }

    @Override
    protected void setOutputStream(OutputStream stream) {
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
}
