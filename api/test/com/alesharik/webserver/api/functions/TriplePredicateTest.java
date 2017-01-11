package com.alesharik.webserver.api.functions;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TriplePredicateTest {
    private TriplePredicate<Integer, Integer, Integer> predicate;
    private TriplePredicate<Integer, Integer, Integer> ok;
    private TriplePredicate<Integer, Integer, Integer> bad;

    @Before
    public void setUp() throws Exception {
        predicate = (integer, integer2, integer3) -> integer.compareTo(integer2) == 0 && integer.compareTo(integer3) == 0;
        ok = (integer, integer2, integer3) -> true;
        bad = (integer, integer2, integer3) -> false;
    }

    @Test
    public void and() throws Exception {
        assertTrue(predicate.and(ok).test(1, 1, 1));
    }

    @Test
    public void badAnd() throws Exception {
        assertFalse(predicate.and(bad).test(1, 1, 1));
    }

    @Test
    public void negate() throws Exception {
        assertFalse(predicate.negate().test(1, 1, 1));
    }

    @Test
    public void badNegate() throws Exception {
        assertTrue(predicate.negate().test(1, 1, 2));
    }

    @Test
    public void or() throws Exception {
        assertTrue(predicate.or(ok).test(1, 1, 1));
        assertTrue(predicate.or(ok).test(1, 1, 2));
    }

    @Test
    public void badOr() throws Exception {
        assertTrue(predicate.or(bad).test(1, 1, 1));
        assertFalse(predicate.or(bad).test(1, 1, 2));
    }

}