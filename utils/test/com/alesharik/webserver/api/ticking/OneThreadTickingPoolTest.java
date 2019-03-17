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

package com.alesharik.webserver.api.ticking;

import org.junit.Before;
import org.junit.Test;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class OneThreadTickingPoolTest {
    private OneThreadTickingPool pool;
    private OneThreadTickingPool not;
    private OneThreadTickingPool not1;
    private OneThreadTickingPool forMBeanTest;
    private Tickable dude = () -> {
    };
    private Tickable imOK = () -> {
    };
    private Tickable sleepy = () -> {
    };

    @Before
    public void setup() {
        pool = new OneThreadTickingPool();

        pool.startTicking(imOK, 501);
        pool.startTicking(sleepy, 999);
        pool.pauseTickable(sleepy);

        not = new OneThreadTickingPool();
        not1 = new OneThreadTickingPool();

        forMBeanTest = new OneThreadTickingPool();
        forMBeanTest.startTicking(() -> {
        }, 1000);
        forMBeanTest.startTicking(() -> {
        }, 1000);
        forMBeanTest.startTicking(() -> {
        }, 1000);
        Tickable paused = () -> {
        };

        forMBeanTest.startTicking(paused, 10);
        forMBeanTest.pauseTickable(paused);
    }

    @Test
    public void getThreadCountTest() {
        assertEquals(1, forMBeanTest.getThreadCount());
    }

    @Test
    public void getTotalTaskCount() {
        assertEquals(4, forMBeanTest.getTotalTaskCount());
    }

    @Test
    public void getRunningTaskCount() {
        assertEquals(3, forMBeanTest.getRunningTaskCount());
    }

    @Test
    public void getPauseTaskCount() {
        assertEquals(1, forMBeanTest.getPauseTaskCount());
    }

    @Test
    public void getIdTest() {
        assertTrue(pool.getId() > 0);
    }

    @Test
    public void testRegisterMBean() throws Exception {
        assertTrue(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.ticking:type=OneThreadTickingPool,id=" + pool.getId())));
        assertTrue(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.ticking:type=OneThreadTickingPool,id=" + not.getId())));
        assertTrue(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.ticking:type=OneThreadTickingPool,id=" + not1.getId())));
        assertTrue(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.ticking:type=OneThreadTickingPool,id=" + forMBeanTest.getId())));
    }

    @Test
    public void finalizeTest() throws Throwable {
        assertTrue(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.ticking:type=OneThreadTickingPool,id=" + not1.getId())));
        not1.getCleaner().clean();
        assertFalse(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.ticking:type=OneThreadTickingPool,id=" + not1.getId())));
    }

    @Test
    public void startTicking() {
        pool.startTicking(dude, 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void startTickingIllegal() {
        pool.startTicking(dude, -1);
    }

    @Test
    public void stopTicking() {
        pool.stopTicking(dude);
    }

    @Test
    public void pauseTickable() {
        pool.pauseTickable(dude);
    }

    @Test
    public void resumeTickable() {
        pool.resumeTickable(dude);
    }

    @Test
    public void isRunning() {
        assertTrue("Running tickable is not running!", pool.isRunning(imOK));
        assertFalse("Sleeping tickable is running!", pool.isRunning(sleepy));

        assertFalse(pool.isRunning(() -> {
        }));
    }

    @Test
    public void shutdown() {
        pool.shutdown();
    }

    @Test
    public void shutdownNow() {
        pool.shutdownNow();
    }

    @Test
    public void workTest() throws InterruptedException {
        OneThreadTickingPool pool1 = new OneThreadTickingPool();
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        pool1.startTicking(() -> atomicBoolean.set(true), 10);
        Thread.sleep(100);
        assertTrue(atomicBoolean.get());
        pool1.shutdown();
    }
}