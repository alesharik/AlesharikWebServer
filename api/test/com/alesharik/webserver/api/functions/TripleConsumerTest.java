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
    public void setUp() throws Exception {
        consumerA = (integer, integer2, atomicInteger) -> atomicInteger.incrementAndGet();
        consumerB = (integer, integer2, atomicInteger) -> atomicInteger.incrementAndGet();
        consumerC = (integer, integer2, atomicInteger) -> atomicInteger.incrementAndGet();
    }

    @Test
    public void andThen() throws Exception {
        AtomicInteger integer = new AtomicInteger(0);
        consumerA.andThen(consumerB).andThen(consumerC).accept(1, 2, integer);
        assertTrue(integer.get() == 3);
    }

}