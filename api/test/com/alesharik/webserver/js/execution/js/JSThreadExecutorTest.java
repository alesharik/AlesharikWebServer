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

package com.alesharik.webserver.js.execution.js;

import jdk.nashorn.api.scripting.AbstractJSObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class JSThreadExecutorTest {
    private PrintStream err;
    private PrintStream mock;

    @Before
    public void setUp() throws Exception {
        err = System.err;
        mock = mock(PrintStream.class);
        System.setErr(mock);
    }

    @After
    public void tearDown() throws Exception {
        System.setErr(err);
    }

    @Test
    public void execute() throws InterruptedException {
        AbstractJSObject object = mock(AbstractJSObject.class);
        AtomicBoolean ok = new AtomicBoolean(false);
        when(object.call(any())).then(invocation -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                ok.set(true);
            }
            return null;
        });

        JSThread thread = new JSThread(object, new Object());

        assertTrue(JSThreadExecutor.threads.isEmpty());

        thread.start();

        assertEquals(1, JSThreadExecutor.threads.size());

        thread.interrupt();
        assertTrue(ok.get());
    }

    @Test
    public void interruptChecker() throws InterruptedException {
        JSThreadExecutor.CheckerThread checkerThread = new JSThreadExecutor.CheckerThread();
        checkerThread.start();
        Thread.sleep(2);
        checkerThread.interrupt();
        Thread.sleep(10);
        verify(mock, times(1)).println("JSThread checker thread is interrupted!");
    }
}