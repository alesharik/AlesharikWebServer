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
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class JSThreadTest {
    @Test
    public void startInterrupt() throws InterruptedException {
        AbstractJSObject object = mock(AbstractJSObject.class);

        JSThread thread = new JSThread(object, new Object());
        assertFalse(thread.isRunning());
        thread.start();
        assertTrue(thread.isRunning());
        Thread.sleep(10);
        thread.interrupt();
        assertFalse(thread.isRunning());

        verify(object, times(1)).call(any());
    }

    @Test
    public void sharedStorage() {
        Object shared = new Object();
        JSThread thread = new JSThread(mock(AbstractJSObject.class), shared);
        assertEquals(shared, thread.getSharedStorage());
    }

    @Test
    public void threadName() {
        JSThread thread = new JSThread(mock(AbstractJSObject.class), new Object());
        Thread real = new Thread();
        thread.setThread(real);

        assertEquals(real.getName(), thread.getName());
        thread.setName("test");
        assertEquals("test", thread.getName());
        assertEquals("test", real.getName());
    }

    @Test
    public void daemon() {
        JSThread thread = new JSThread(mock(AbstractJSObject.class), new Object());
        Thread real = new Thread();
        thread.setThread(real);

        assertEquals(real.isDaemon(), thread.isDaemon());

        thread.setDaemon(true);
        assertTrue(real.isDaemon());
        assertTrue(thread.isDaemon());

        thread.setDaemon(false);
        assertFalse(real.isDaemon());
        assertFalse(thread.isDaemon());
    }
}