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

import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.ErrorManager;

import static org.junit.Assert.assertEquals;

public class PrintStreamErrorManagerTest {
    private PrintStreamErrorManager errorManager;
    private PrintStreamSpy listener;

    @Before
    public void setUp() throws Exception {
        listener = new PrintStreamSpy();
        errorManager = new PrintStreamErrorManager(listener);
    }

    @Test
    public void errorWithMessageCode() throws Exception {
        errorManager.error(null, null, ErrorManager.GENERIC_FAILURE);
        assertEquals("[LOGGER][ERROR]: " + ErrorManager.GENERIC_FAILURE, listener.take());
    }

    @Test
    public void errorWithMessage() throws Exception {
        errorManager.error("test", null, ErrorManager.GENERIC_FAILURE);
        assertEquals("[LOGGER][ERROR]: " + ErrorManager.GENERIC_FAILURE + ": test", listener.take());
    }

    @Test
    public void errorWithException() throws Exception {
        Exception ex = new Exception();
        errorManager.error(null, ex, ErrorManager.GENERIC_FAILURE);
        assertEquals("[LOGGER][ERROR]: " + ErrorManager.GENERIC_FAILURE, listener.take());

        assertEquals(ex.toString(), listener.take());
        for(StackTraceElement stackTraceElement : ex.getStackTrace())
            assertEquals("\tat " + stackTraceElement.toString(), listener.take());
    }

    @Test
    public void errorWithMessageAndException() throws Exception {
        Exception ex = new Exception();
        errorManager.error("test", ex, ErrorManager.GENERIC_FAILURE);
        assertEquals("[LOGGER][ERROR]: " + ErrorManager.GENERIC_FAILURE + ": test", listener.take());

        assertEquals(ex.toString(), listener.take());
        for(StackTraceElement stackTraceElement : ex.getStackTrace())
            assertEquals("\tat " + stackTraceElement.toString(), listener.take());

    }

    private static final class PrintStreamSpy extends PrintStream {
        private final Deque<String> messages;

        public PrintStreamSpy() throws UnsupportedEncodingException {
            super(System.out, false, "UTF-8");
            this.messages = new ArrayDeque<>();
        }

        @Override
        public void println(String x) {
            messages.add(x);
        }

        @Override
        public void println(Object x) {
            messages.add(x.toString());
        }

        public String take() {
            return messages.isEmpty() ? "" : messages.poll();
        }
    }
}