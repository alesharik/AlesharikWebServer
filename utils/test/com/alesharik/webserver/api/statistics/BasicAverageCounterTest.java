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

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class BasicAverageCounterTest {
    private BasicAverageCounter averageCounter;

    @Before
    public void setUp() {
        averageCounter = new BasicAverageCounter();
    }

    @Test
    public void testWorking() throws Exception {
        averageCounter.setTimePeriod(1, TimeUnit.SECONDS);

        averageCounter.addUnit(500);
        averageCounter.addUnit(100);

        Thread.sleep(1000);

        averageCounter.addUnit(100);
        assertEquals(300, averageCounter.getAverage());

        averageCounter.addUnit(700);

        Thread.sleep(900);

        averageCounter.update();
        assertEquals(400, averageCounter.getAverage());

        averageCounter.reset();

        assertEquals(0, averageCounter.getAverage());
    }
}