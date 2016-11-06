package com.alesharik.webserver.api;

import org.junit.Test;

import static org.junit.Assert.*;

public class LoginPasswordCoderTest {
    @Test
    public void encode() throws Exception {
        try {
            LoginPasswordCoder.encode(null, null);
            fail();
        } catch (NullPointerException e) {
            //OK
        }
        assertTrue("aaddmmiinn".equals(LoginPasswordCoder.encode("admin", "admin")));
        assertTrue("aaddmmiinna".equals(LoginPasswordCoder.encode("admina", "admin")));
        assertTrue("aaddmmiinnaa".equals(LoginPasswordCoder.encode("admin", "adminaa")));
    }

    @Test
    public void isEquals() throws Exception {
        assertTrue(LoginPasswordCoder.isEquals(null, null, null));
        assertFalse(LoginPasswordCoder.isEquals(null, null, "asd"));
        assertFalse(LoginPasswordCoder.isEquals(null, "asd", null));
        assertFalse(LoginPasswordCoder.isEquals("asd", null, null));
        assertFalse(LoginPasswordCoder.isEquals("asd", "asd", null));
        assertFalse(LoginPasswordCoder.isEquals("asd", null, "asd"));
        assertTrue(LoginPasswordCoder.isEquals("admin", "admin", "aaddmmiinn"));
        assertFalse(LoginPasswordCoder.isEquals("admin", "admina", "aaddmmiinn"));
    }

}