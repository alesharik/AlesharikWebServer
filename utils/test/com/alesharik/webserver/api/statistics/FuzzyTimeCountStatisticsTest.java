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

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class FuzzyTimeCountStatisticsTest {
    @Test
    public void measureTest() throws Exception {
        TimeCountStatistics statistics = new FuzzyTimeCountStatistics(100, TimeUnit.MILLISECONDS);
        assertEquals(statistics.get(), 0);
        statistics.measure(100);

        Thread.sleep(100);
        statistics.measure(50);

        assertEquals(100, statistics.get());

        Thread.sleep(100);
        statistics.measure(100);
        assertEquals(50, statistics.get());
    }

    @Test
    public void updateTest() throws Exception {
        TimeCountStatistics statistics = new FuzzyTimeCountStatistics(100, TimeUnit.MILLISECONDS);
        assertEquals(statistics.get(), 0);
        statistics.measure(100);

        Thread.sleep(100);
        statistics.update();

        assertEquals(100, statistics.get());
        Thread.sleep(100);

        statistics.update();
        assertEquals(0, statistics.get());
    }
}