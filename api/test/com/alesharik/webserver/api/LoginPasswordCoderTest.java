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

package com.alesharik.webserver.api;

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