/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.logger.logger;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
    private AtomicReference<Charset> charset = new AtomicReference<>(StandardCharsets.UTF_8);

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
        this(file, StandardCharsets.UTF_8);
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
    public synchronized String getEncoding() {
        if(this.charset == null)
            this.charset = new AtomicReference<>(StandardCharsets.UTF_8);
        if(this.charset.get() == null)
            this.charset.set(StandardCharsets.UTF_8);
        return this.charset.get().toString();
    }

    @Override
    public synchronized void setEncoding(String encoding) throws SecurityException {
        Charset charset = Charset.forName(encoding);
        this.charset.set(charset);
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
