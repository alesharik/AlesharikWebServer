package com.alesharik.webserver.api.functions;

import org.junit.Before;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertTrue;

public class TripleFunctionTest {
    private TripleFunction<Integer, Integer, Integer, Integer> functionA;
    private Function<Integer, Integer> functionB;

    @Before
    public void setUp() throws Exception {
        functionA = (integer, integer2, integer3) -> integer + integer2 + integer3;
        functionB = integer -> integer + 1;
    }

    @Test
    public void andThen() throws Exception {
        assertTrue((functionA.andThen(functionB).apply(1, 2, 3)) == 7);
    }

}