package com.alesharik.webserver.api;

import com.alesharik.webserver.TestUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class LoginPasswordCoderTest {
    @Test
    public void encodeSameLength() throws Exception {
        assertEquals("aaddmmiinn", LoginPasswordCoder.encode("admin", "admin"));
    }

    @Test
    public void encodeDifferentLength() throws Exception {
        assertEquals("hhiello", LoginPasswordCoder.encode("hi", "hello"));
        assertEquals("hheillo", LoginPasswordCoder.encode("hello", "hi"));
    }

    @Test
    public void isEqualsNull() throws Exception {
        assertTrue(LoginPasswordCoder.isEquals(null, null, null));
    }

    @Test
    public void isEqualsNullButOne() throws Exception {
        assertFalse(LoginPasswordCoder.isEquals(null, "a", "a"));
        assertFalse(LoginPasswordCoder.isEquals("a", null, "a"));
        assertFalse(LoginPasswordCoder.isEquals("a", "a", null));

        assertFalse(LoginPasswordCoder.isEquals("a", null, null));
        assertFalse(LoginPasswordCoder.isEquals(null, "a", null));
        assertFalse(LoginPasswordCoder.isEquals(null, null, "a"));
    }

    @Test
    public void isEqualsAll() throws Exception {
        assertTrue(LoginPasswordCoder.isEquals("admin", "admin", "aaddmmiinn"));
        assertFalse(LoginPasswordCoder.isEquals("admin", "test", "aaddmmiinn"));
    }

    @Test
    public void testUtilityClass() throws Exception {
        TestUtils.assertUtilityClass(LoginPasswordCoder.class);
    }
}