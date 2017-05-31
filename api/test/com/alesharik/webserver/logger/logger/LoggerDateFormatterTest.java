package com.alesharik.webserver.logger.logger;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.Assert.assertEquals;

public class LoggerDateFormatterTest {
    private LoggerDateFormatter formatter;

    @Before
    public void setUp() throws Exception {
        formatter = new LoggerDateFormatter();
    }

    @Test
    public void format() throws Exception {
        LogRecord logRecord = new LogRecord(Level.FINE, "test");

        long millis = System.currentTimeMillis();
        logRecord.setMillis(millis);

        String expected = "[Date: " + new Date(millis).toString() + "]test\n";
        assertEquals(expected, formatter.format(logRecord));
    }

    @Test
    public void formatMessage() throws Exception {
        LogRecord logRecord = new LogRecord(Level.FINE, "test");

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