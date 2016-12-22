package tests.com.alesharik.webserver.api.ticking;

import com.alesharik.webserver.api.ticking.OneThreadTickingPool;
import com.alesharik.webserver.api.ticking.Tickable;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OneThreadTickingPoolTest {
    private OneThreadTickingPool pool;
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
    }

    @Test
    public void startTicking() throws Exception {
        pool.startTicking(dude, 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void startTickingIllegal() throws Exception {
        pool.startTicking(dude, -1);
    }

    @Test(expected = NullPointerException.class)
    public void startTickingNull() throws Exception {
        pool.startTicking(null, 1000);
    }

    @Test
    public void stopTicking() throws Exception {
        pool.stopTicking(dude);
    }

    @Test(expected = NullPointerException.class)
    public void stopTickingNull() throws Exception {
        pool.stopTicking(null);
    }

    @Test
    public void stopTicking1() throws Exception {
        pool.stopTicking(dude, 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void stopTicking1Illegal() throws Exception {
        pool.stopTicking(dude, -1);
    }

    @Test(expected = NullPointerException.class)
    public void stopTicking1Null() throws Exception {
        pool.stopTicking(null, 1000);
    }

    @Test
    public void pauseTickable() throws Exception {
        pool.pauseTickable(dude);
    }

    @Test(expected = NullPointerException.class)
    public void pauseTickableNull() throws Exception {
        pool.pauseTickable(null);
    }

    @Test
    public void resumeTickable() throws Exception {
        pool.resumeTickable(dude);
    }

    @Test(expected = NullPointerException.class)
    public void resumeTickableNull() throws Exception {
        pool.resumeTickable(null);
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
    public void z_shutdown() throws Exception { //z for last execution
        pool.shutdown();
    }

    @Test
    public void z_shutdownNow() throws Exception { //z for last execution
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