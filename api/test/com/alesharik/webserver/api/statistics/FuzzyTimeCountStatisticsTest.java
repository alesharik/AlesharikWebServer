package com.alesharik.webserver.api.statistics;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class FuzzyTimeCountStatisticsTest {
    @Test
    public void measureTest() throws Exception {
        TimeCountStatistics statistics = new FuzzyTimeCountStatistics(100, TimeUnit.MILLISECONDS);
        assertEquals(statistics.getCount(), 0);
        statistics.measure(100);

        Thread.sleep(100);
        statistics.measure(50);

        assertEquals(100, statistics.getCount());

        Thread.sleep(100);
        statistics.measure(100);
        assertEquals(50, statistics.getCount());
    }

    @Test
    public void updateTest() throws Exception {
        TimeCountStatistics statistics = new FuzzyTimeCountStatistics(100, TimeUnit.MILLISECONDS);
        assertEquals(statistics.getCount(), 0);
        statistics.measure(100);

        Thread.sleep(100);
        statistics.update();

        assertEquals(100, statistics.getCount());
        Thread.sleep(100);

        statistics.update();
        assertEquals(0, statistics.getCount());
    }
}