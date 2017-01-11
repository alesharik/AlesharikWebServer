package com.alesharik.webserver.api.ticking;

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
}