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

package com.alesharik.webserver.api.functions;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

public class TripleConsumerTest {
    private TripleConsumer<Integer, Integer, AtomicInteger> consumerA;
    private TripleConsumer<Integer, Integer, AtomicInteger> consumerB;
    private TripleConsumer<Integer, Integer, AtomicInteger> consumerC;

    @Before
    public void setUp() {
        consumerA = (integer, integer2, atomicInteger) -> atomicInteger.incrementAndGet();
        consumerB = (integer, integer2, atomicInteger) -> atomicInteger.incrementAndGet();
        consumerC = (integer, integer2, atomicInteger) -> atomicInteger.incrementAndGet();
    }

    @Test
    public void andThen() {
        AtomicInteger integer = new AtomicInteger(0);
        consumerA.andThen(consumerB).andThen(consumerC).accept(1, 2, integer);
        assertTrue(integer.get() == 3);
    }

}