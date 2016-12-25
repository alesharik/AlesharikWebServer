package tests.com.alesharik.webserver.api.ticking;

import com.alesharik.webserver.api.ticking.OneThreadTickingPool;
import com.alesharik.webserver.api.ticking.Tickable;
import com.alesharik.webserver.api.ticking.TickingPool;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TickingPoolTest {
    private TickingPool pool;
    private Tickable dude = () -> {
    };

    @Before
    public void setUp() throws Exception {
        pool = new OneThreadTickingPool();
    }

    @Test
    public void startTicking() throws Exception {
        pool.startTicking(dude, 100, TimeUnit.SECONDS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void startTickingIllegal() throws Exception {
        pool.startTicking(dude, -100, TimeUnit.SECONDS);
    }

    @Test(expected = NullPointerException.class)
    public void startTickingNull() throws Exception {
        pool.startTicking(dude, 100, null);
    }
}