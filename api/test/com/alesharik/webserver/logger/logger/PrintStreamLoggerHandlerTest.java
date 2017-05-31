package com.alesharik.webserver.logger.logger;

import org.glassfish.grizzly.utils.Charsets;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.Assert.*;

public class PrintStreamLoggerHandlerTest {
    private PrintStreamLoggerHandler consoleHandler;
    private PrintStreamSpy streamSpy;

    @Before
    public void setUp() throws Exception {
        consoleHandler = new PrintStreamLoggerHandler();
        streamSpy = new PrintStreamSpy();

        consoleHandler.setEncoding("UTF-8");
        consoleHandler.setOutputStream(streamSpy);
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return record.getMessage();
            }
        });
    }

    @Test
    public void testIsLoggableNull() throws Exception {
        assertFalse(consoleHandler.isLoggable(null));
    }

    @Test
    public void testIsLoggableNotCorrectLevel() throws Exception {
        consoleHandler.setLevel(Level.FINE);
        LogRecord logRecord = new LogRecord(Level.FINER, "");
        assertFalse(consoleHandler.isLoggable(logRecord));
    }

    @Test
    public void testIsLoggableFilter() throws Exception {
        consoleHandler.setFilter(record -> false);
        assertFalse(consoleHandler.isLoggable(new LogRecord(Level.FINE, "")));
    }

    @Test
    public void testIsLoggableCorrect() throws Exception {
        assertTrue(consoleHandler.isLoggable(new LogRecord(Level.FINE, "")));
    }

    @Test
    public void testPublish() throws Exception {
        consoleHandler.publish(new LogRecord(Level.FINER, "test"));
        assertEquals("test", streamSpy.take());
    }

    @Test
    public void testSetOutputStream() throws Exception {
        consoleHandler.setOutputStream(streamSpy);
        consoleHandler.publish(new LogRecord(Level.FINER, "test"));
        assertEquals("test", streamSpy.take());
    }

    private static final class PrintStreamSpy extends PrintStream {
        private final Deque<String> messages;

        public PrintStreamSpy() {
            super(System.out);
            messages = new ArrayDeque<>();
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            messages.add(new String(buf, Charsets.UTF8_CHARSET).replaceAll("\u0000", ""));
        }

        public String take() {
            return messages.isEmpty() ? "" : messages.poll();
        }
    }
}