package com.alesharik.webserver.api;

import org.junit.Test;

import static org.junit.Assert.*;


public class UtilsTest {
    @Test
    public void getExternalIp() throws Exception {
        Utils.getExternalIp();
    }

    @Test
    public void requireNotNullOrEmpty() throws Exception {
        Utils.requireNotNullOrEmpty("asd");
        try {
            Utils.requireNotNullOrEmpty(null);
            fail();
        } catch (NullPointerException e) {
            //OK
        }
        try {
            Utils.requireNotNullOrEmpty("");
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void getCoresCount() throws Exception {
        Utils.getCoresCount();
    }

    @Test
    public void getCoreInfo() throws Exception {
        Utils.getCoreInfo(0);
    }

    @Test
    public void getRAMInfo() throws Exception {
        Utils.getRAMInfo();
    }

    @Test
    public void getURLAsByteArray() throws Exception {
        try {
            Utils.getURLAsByteArray(null);
            fail();
        } catch (NullPointerException e) {
            //PK
        }
    }

    @Test
    public void notNullAndEmpty() throws Exception {
        assertTrue("asd".equals(Utils.notNullAndEmpty("asd")));
        assertFalse("asd".equals(Utils.notNullAndEmpty("asdf")));
        try {
            Utils.notNullAndEmpty(null);
            fail();
        } catch (NullPointerException e) {
            //OK
        }
        try {
            Utils.notNullAndEmpty("");
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }
    }
}