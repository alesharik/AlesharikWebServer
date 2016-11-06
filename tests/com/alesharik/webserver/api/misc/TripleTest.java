package com.alesharik.webserver.api.misc;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TripleTest {
    @Test
    public void immutable() throws Exception {
        Triple<String, String, String> triple = Triple.immutable("asd", "asd", "asd");
    }

    @Test
    public void getA() throws Exception {
        Triple<String, String, String> triple = Triple.immutable("asd", "asd", "asd");
        assertTrue("asd".equals(triple.getA()));
    }

    @Test
    public void getB() throws Exception {
        Triple<String, String, String> triple = Triple.immutable("asd", "asd", "asd");
        assertTrue("asd".equals(triple.getB()));
    }

    @Test
    public void getC() throws Exception {
        Triple<String, String, String> triple = Triple.immutable("asd", "asd", "asd");
        assertTrue("asd".equals(triple.getC()));
    }

    @Test
    public void equals() throws Exception {
        Triple<String, String, String> triple = Triple.immutable("asd", "asd", "asd");
        Triple<String, String, String> triple1 = Triple.immutable("asd", "asd", "asd");
        Triple<String, String, String> triple2 = Triple.immutable("asdd", "asda", "asda");
        assertTrue(triple.equals(triple1));
        assertFalse(triple.equals(triple2));
        assertFalse(triple1.equals(triple2));
    }

    @Test
    public int hashCode() {

        return 0;
    }

    @Test
    public String toString() {

        return null;
    }

}