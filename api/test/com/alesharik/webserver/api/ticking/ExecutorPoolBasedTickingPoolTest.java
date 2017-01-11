package com.alesharik.webserver.api.ticking;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Before;
import org.junit.Test;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class ExecutorPoolBasedTickingPoolTest {
    private ExecutorPoolBasedTickingPool pool;
    private ExecutorPoolBasedTickingPool not;
    private ExecutorPoolBasedTickingPool not1;
    private ExecutorPoolBasedTickingPool forMBeanTest;
    private Tickable dude = () -> {
    };
    private Tickable imOK = () -> {
    };
    private Tickable sleepy = () -> {
    };

    @Before
    public void setup() {
        pool = new ExecutorPoolBasedTickingPool();

        pool.startTicking(imOK, 501);
        pool.startTicking(sleepy, 999);
        pool.pauseTickable(sleepy);

        not = new ExecutorPoolBasedTickingPool();

        not1 = new ExecutorPoolBasedTickingPool();

        forMBeanTest = new ExecutorPoolBasedTickingPool(5);
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
    public void getThreadCountTest() throws Exception {
        assertTrue(forMBeanTest.getThreadCount() == 5);
    }

    @Test
    public void getTotalTaskCount() throws Exception {
        assertTrue(forMBeanTest.getTotalTaskCount() == 4);
    }

    @Test
    public void getRunningTaskCount() throws Exception {
        assertTrue(forMBeanTest.getRunningTaskCount() == 3);
    }

    @Test
    public void getPauseTaskCount() throws Exception {
        assertTrue(forMBeanTest.getPauseTaskCount() == 1);
    }

    @Test
    public void getIdTest() throws Exception {
        assertTrue(pool.getId() > 0);
    }

    @Test
    public void testRegisterMBean() throws Exception {
        assertTrue(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.ticking:type=ExecutorPoolBasedTickingPool,id=" + pool.getId())));
        assertTrue(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.ticking:type=ExecutorPoolBasedTickingPool,id=" + not.getId())));
        assertTrue(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.ticking:type=ExecutorPoolBasedTickingPool,id=" + not1.getId())));
        assertTrue(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.ticking:type=ExecutorPoolBasedTickingPool,id=" + forMBeanTest.getId())));
    }

    @SuppressFBWarnings("FI_EXPLICIT_INVOCATION")
    @SuppressWarnings("FinalizeCalledExplicitly")
    @Test
    public void finalizeTest() throws Throwable {
        assertTrue(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.ticking:type=ExecutorPoolBasedTickingPool,id=" + not1.getId())));
        not1.finalize();
        assertFalse(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.ticking:type=ExecutorPoolBasedTickingPool,id=" + not1.getId())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void newPoolWithParallelismZero() throws Exception {
        new ExecutorPoolBasedTickingPool(0);
    }

    @Test
    public void startTicking() throws Exception {
        pool.startTicking(dude, 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void startTickingIllegal() throws Exception {
        pool.startTicking(dude, -1);
    }

    @Test
    public void stopTicking() throws Exception {
        pool.stopTicking(dude);
    }

    @Test
    public void pauseTickable() throws Exception {
        pool.pauseTickable(dude);
    }
    @Test
    public void resumeTickable() throws Exception {
        pool.resumeTickable(dude);
    }

    @Test
    public void isRunning() throws Exception {
        assertTrue("Running tickable is not running!", pool.isRunning(imOK));
    }

    @Test
    public void isRunning1() throws Exception {
        assertFalse("Sleeping tickable is running!", pool.isRunning(sleepy));
    }

    @Test
    public void shutdown() throws Exception {
        pool.shutdown();
    }

    @Test
    public void shutdownNow() throws Exception {
        pool.shutdownNow();
    }

    @Test
    public void workTest() throws InterruptedException {
        ExecutorPoolBasedTickingPool pool1 = new ExecutorPoolBasedTickingPool();
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        pool1.startTicking(() -> atomicBoolean.set(true), 10);
        Thread.sleep(100);
        assertTrue(atomicBoolean.get());
        pool1.shutdown();
    }

    @Test
    public void equals() throws Exception {
        assertFalse(pool.equals(not));
        assertFalse(not.equals(not1));
    }

    @Test
    public void hashCodeTest() throws Exception {
        assertTrue(Integer.compare(pool.hashCode(), not.hashCode()) != 0);
    }

    @Test
    public void toStringTest() throws Exception {
        assertNotNull(pool.toString());
    }
}