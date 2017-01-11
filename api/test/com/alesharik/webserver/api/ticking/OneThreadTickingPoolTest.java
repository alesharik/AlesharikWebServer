package com.alesharik.webserver.api.ticking;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class OneThreadTickingPoolTest {
    private OneThreadTickingPool pool;
    private OneThreadTickingPool not;
    private OneThreadTickingPool not1;
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
        OneThreadTickingPool pool1 = new OneThreadTickingPool();
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