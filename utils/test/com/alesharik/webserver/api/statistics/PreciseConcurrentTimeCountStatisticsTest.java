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

package com.alesharik.webserver.api.statistics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertEquals;

public class PreciseConcurrentTimeCountStatisticsTest {
    private PreciseConcurrentTimeCountStatistics statistics;
    private ScheduledExecutorService timer;

    @Before
    public void setUp() throws Exception {
        timer = Executors.newSingleThreadScheduledExecutor();
        statistics = new PreciseConcurrentTimeCountStatistics(1, timer);
    }

    @After
    public void tearDown() throws Exception {
        timer.shutdown();
    }

    @Test
    public void testLogic() throws Exception {
        statistics.measure(1);
        statistics.measure(100);
        assertEquals(101, statistics.getCount());
        Thread.sleep(2);
        statistics.update();
        assertEquals(0, statistics.getCount());
    }
}