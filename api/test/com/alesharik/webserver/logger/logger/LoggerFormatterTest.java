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

import com.alesharik.webserver.logger.Logger;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.Assert.assertEquals;

public class LoggerFormatterTest {
    private LoggerFormatter formatter;

    @Before
    public void setUp() throws Exception {
        formatter = new LoggerFormatter();
    }

    @Test
    public void format() throws Exception {
        LogRecord logRecord = new LogRecord(Level.FINE, new Logger.TextFormatter(Logger.ForegroundColor.MAGENTA, Logger.BackgroundColor.BLACK)
                .format("test"));

        long millis = System.currentTimeMillis();
        logRecord.setMillis(millis);

        String expected = "[Date: " + new Date(millis).toString() + "]test\n";
        assertEquals(expected, formatter.format(logRecord));
    }

    @Test
    public void formatMessage() throws Exception {
        LogRecord logRecord = new LogRecord(Level.FINE, new Logger.TextFormatter(Logger.ForegroundColor.MAGENTA, Logger.BackgroundColor.BLACK)
                .format("test"));

        long millis = System.currentTimeMillis();
        logRecord.setMillis(millis);

        String expected = "[Date: " + new Date(millis).toString() + "]test\n";
        assertEquals(expected, formatter.formatMessage(logRecord));
    }

    @Test
    public void getHead() throws Exception {
        assertEquals("", formatter.getHead(null));
        assertEquals("", formatter.getHead(new ConsoleHandler()));
    }

    @Test
    public void getTail() throws Exception {
        assertEquals("", formatter.getHead(null));
        assertEquals("", formatter.getHead(new ConsoleHandler()));
    }
}