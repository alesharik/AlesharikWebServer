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

package com.alesharik.webserver.api;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class DebounceManagerTest {
    @BeforeClass
    public static void setup() {
        DebounceManager.remove(new Object());
    }

    @Test
    public void debounce() throws InterruptedException, MalformedObjectNameException {
        DebounceManager.MXBean mxBean = JMX.newMBeanProxy(ManagementFactory.getPlatformMBeanServer(), new ObjectName("com.alesharik.webserver.api:type=DebounceManager"), DebounceManager.MXBean.class);
        assertTrue(mxBean.isRunning());
        Runnable test = mock(Runnable.class);
        Object key = new Object();
        DebounceManager.debounce(key, test, 1, TimeUnit.SECONDS);
        verify(test, never()).run();
        assertEquals(1, mxBean.getTaskCount());
        Thread.sleep(300);
        verify(test, never()).run();
        assertEquals(1, mxBean.getTaskCount());
        DebounceManager.debounce(key, test, 100, TimeUnit.MILLISECONDS);
        verify(test, never()).run();
        assertEquals(1, mxBean.getTaskCount());
        Thread.sleep(110);
        verify(test, times(1)).run();
        assertEquals(0, mxBean.getTaskCount());
    }

    @Test
    public void debounceMultiple() throws InterruptedException {
        List<Runnable> tests = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            Runnable runnable = mock(Runnable.class);
            DebounceManager.debounce(new Object(), runnable, 10, TimeUnit.MILLISECONDS);
            tests.add(runnable);
        }
        Thread.sleep(20);
        for(Runnable test : tests)
            verify(test, times(1)).run();
    }

    @Test
    public void remove() throws MalformedObjectNameException {
        DebounceManager.MXBean bean = JMX.newMBeanProxy(ManagementFactory.getPlatformMBeanServer(), new ObjectName("com.alesharik.webserver.api:type=DebounceManager"), DebounceManager.MXBean.class);
        assertTrue(bean.isRunning());
        Object key = new Object();
        Runnable test = mock(Runnable.class);
        DebounceManager.debounce(key, test, 100, TimeUnit.MILLISECONDS);
        assertEquals(1, bean.getTaskCount());
        DebounceManager.remove(key);
        assertEquals(0, bean.getTaskCount());
        verify(test, never()).run();
    }

    @Test
    public void removeNotExisting() {
        DebounceManager.remove(new Object());
    }
}