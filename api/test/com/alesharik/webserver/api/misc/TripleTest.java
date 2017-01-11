package com.alesharik.webserver.api.misc;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TripleTest {
    private Triple<Integer, Integer, Integer> test;
    private Triple<Integer, Integer, Integer> same;
    private Triple<Integer, Integer, Integer> notSame;

    @Before
    public void setUp() throws Exception {
        test = Triple.immutable(2133, 2134, 2135);
        same = Triple.immutable(2133, 2134, 2135);
        notSame = Triple.immutable(1243, 4320, 43201);
    }

    @Test
    public void getATest() throws Exception {
        assertTrue(test.getA() == 2133);
    }

    @Test
    public void getBTest() throws Exception {
        assertTrue(test.getB() == 2134);
    }

    @Test
    public void getCTest() throws Exception {
        assertTrue(test.getC() == 2135);
    }

    @Test
    public void equalsSameTest() throws Exception {
        assertTrue(test.equals(same));
    }

    @Test
    public void notEqualsTest() throws Exception {
        assertFalse(test.equals(notSame));
    }

    @Test
    public void equalsHashCodeTest() throws Exception {
        assertTrue(Integer.compare(test.hashCode(), same.hashCode()) == 0);
    }

    @Test
    public void notEqualsHashCodeTest() throws Exception {
        assertTrue(Integer.compare(test.hashCode(), notSame.hashCode()) != 0);
    }

    @Test
    public void toStringTest() throws Exception {
        assertNotNull(test.toString());
    }
}