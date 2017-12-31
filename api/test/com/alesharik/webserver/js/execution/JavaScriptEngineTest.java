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

package com.alesharik.webserver.js.execution;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class JavaScriptEngineTest {
    private static volatile boolean ok = false;
    private JavaScriptEngine engine;

    @Before
    public void setUp() throws Exception {
        engine = new JavaScriptEngine(false, true);
        ok = false;
    }

    @Test
    public void inputListeners() {
        JavaScriptInputListener listener = mock(JavaScriptInputListener.class);
        assertFalse(engine.containsInputListener(listener));
        engine.addInputListener(listener);
        assertTrue(engine.containsInputListener(listener));
        engine.removeInputListener(listener);
        assertFalse(engine.containsInputListener(listener));
    }

    @Test
    public void outputListeners() {
        JavaScriptOutputListener listener = mock(JavaScriptOutputListener.class);
        assertFalse(engine.containsOutputListener(listener));
        engine.addOutputListener(listener);
        assertTrue(engine.containsOutputListener(listener));
        engine.removeOutputListener(listener);
        assertFalse(engine.containsOutputListener(listener));
    }

    @Test
    public void exceptionHandler() {
        JavaScriptExceptionHandler handler = mock(JavaScriptExceptionHandler.class);
        assertNotNull(engine.getExceptionHandler());
        engine.setExceptionHandler(handler);
        assertSame(handler, engine.getExceptionHandler());
    }

    @Test
    public void testExec() throws ScriptException {
        JavaScriptInputListener inputListener = mock(JavaScriptInputListener.class);
        JavaScriptOutputListener outputListener = mock(JavaScriptOutputListener.class);

        engine.addOutputListener(outputListener);
        engine.addInputListener(inputListener);

        engine.execute("print('test')");

        verify(inputListener).listen("print('test')");
        verify(outputListener).listen("test\n");
    }

    @Test
    public void testExecFile() throws ScriptException, IOException {
        JavaScriptInputListener inputListener = mock(JavaScriptInputListener.class);
        JavaScriptOutputListener outputListener = mock(JavaScriptOutputListener.class);

        engine.addOutputListener(outputListener);
        engine.addInputListener(inputListener);
        File f = File.createTempFile("asdsdasdasad", "asdasdasdas");
        Files.write(f.toPath(), "print('test')".getBytes("UTF-8"));
        engine.load(f);

        verify(inputListener).listenFile(f);
        verify(outputListener).listen("test\n");
    }

    @Test
    public void testExecException() throws ScriptException {
        JavaScriptInputListener inputListener = mock(JavaScriptInputListener.class);
        JavaScriptExceptionHandler exceptionHandler = mock(JavaScriptExceptionHandler.class);

        engine.setExceptionHandler(exceptionHandler);
        engine.addInputListener(inputListener);

        ScriptException s = null;
        try {
            engine.execute("print(undefined.asd)");
        } catch (ScriptException e) {
            s = e;
        }

        verify(inputListener).listen("print(undefined.asd)");
        ArgumentCaptor<ScriptException> captor = ArgumentCaptor.forClass(ScriptException.class);
        verify(exceptionHandler).handle(captor.capture());
        ScriptException val = captor.getValue();

        assertEquals("TypeError: Cannot read property \"asd\" from undefined in <eval> at line number 1", val.getMessage());
        assertSame(s, val);
    }

    @Test
    public void testExecFileException() throws ScriptException, IOException {
        JavaScriptInputListener inputListener = mock(JavaScriptInputListener.class);
        JavaScriptExceptionHandler exceptionHandler = mock(JavaScriptExceptionHandler.class);

        engine.setExceptionHandler(exceptionHandler);
        engine.addInputListener(inputListener);

        File f = File.createTempFile("asdsdasdasad", "asdasdasdas");
        Files.write(f.toPath(), "print(undefined.asd)".getBytes("UTF-8"));
        ScriptException s = null;
        try {
            engine.load(f);
        } catch (ScriptException e) {
            s = e;
        }

        verify(inputListener).listenFile(f);
        ArgumentCaptor<ScriptException> captor = ArgumentCaptor.forClass(ScriptException.class);
        verify(exceptionHandler).handle(captor.capture());
        ScriptException val = captor.getValue();

        assertEquals("TypeError: Cannot read property \"asd\" from undefined in <eval> at line number 1", val.getMessage());
        assertSame(s, val);
    }

    @Test
    public void ctorTest() {
        new JavaScriptEngine(true, true);
        new JavaScriptEngine(true, false);
    }

    @Test
    public void threadApiTest() throws ScriptException, InterruptedException {
        engine.execute("var thread = new Thread(function() {" +
                "Java.type(\"com.alesharik.webserver.js.execution.JavaScriptEngineTest\").ok()" +
                "}, {});" +
                "thread.start();");
        Thread.sleep(100);
        assertTrue(ok);
    }

    public static void ok() {
        ok = true;
    }
}