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
    public synchronized String getEncoding() {
        return charset.toString();
    }
}
