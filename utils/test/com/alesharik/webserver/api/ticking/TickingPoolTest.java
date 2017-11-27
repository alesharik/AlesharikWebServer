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

import java.util.concurrent.TimeUnit;

public class TickingPoolTest {
    private TickingPool pool;
    private Tickable dude = () -> System.out.println("Hi!");

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