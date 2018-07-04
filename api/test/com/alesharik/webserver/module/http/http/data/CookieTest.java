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

package com.alesharik.webserver.module.http.http.data;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CookieTest {
    private final String OLD_DATE;
    private final SimpleDateFormat OLD_COOKIE_DATE_FORMAT;

    {
        OLD_COOKIE_DATE_FORMAT = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US);
        OLD_COOKIE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        OLD_DATE = OLD_COOKIE_DATE_FORMAT.format(new Date(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNameAndWhitespaceCharacter() throws Exception {
        Cookie cookie = new Cookie("asdasd asdasdsdasd", "");
        assertNull(cookie);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNameAndEqualsCharacter() throws Exception {
        Cookie cookie = new Cookie("asd=dsadasd", "");
        assertNull(cookie);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNameAndSemicolonCharacter() throws Exception {
        Cookie cookie = new Cookie("asd;asd", "");
        assertNull(cookie);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNameAndCommaCharacter() throws Exception {
        Cookie cookie = new Cookie("asd,sad", "");
        assertNull(cookie);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithValueAndWhitespaceCharacter() throws Exception {
        Cookie cookie = new Cookie("", "asdasd asdasdsdasd");
        assertNull(cookie);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithValueAndSemicolonCharacter() throws Exception {
        Cookie cookie = new Cookie("", "asd;asd");
        assertNull(cookie);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithValueAndCommaCharacter() throws Exception {
        Cookie cookie = new Cookie("", "asd,sad");
        assertNull(cookie);
    }

    @Test
    public void toCookieStringNew() throws Exception {
        Cookie cookie = new Cookie("test", "test");
        cookie.setComment("comment");
        cookie.setDomain(".bar.foo");
        cookie.setPath("/test");
        cookie.setMaxAge(1);
        cookie.setSecure(true);
        cookie.setVersion(1);
        cookie.setHttpOnly(true);
        String excepted = "test=test; Max-Age=1; Comment=comment; Domain=.bar.foo; Path=/test; Secure; Version=1; HttpOnly";
        assertEquals(excepted, cookie.toCookieString());
    }

    @Test
    public void toCookieStringOldWithMaxAgeEqualsZero() throws Exception {
        Cookie cookie = new Cookie("test", "test");
        cookie.setComment("comment");
        cookie.setDomain(".bar.foo");
        cookie.setPath("/test");
        cookie.setMaxAge(0);
        cookie.setSecure(true);
        cookie.setVersion(0);
        cookie.setHttpOnly(true);
        String excepted = "test=test; Expires=" + OLD_DATE + "; Domain=.bar.foo; Path=/test; Secure; HttpOnly";
        assertEquals(excepted, cookie.toCookieString());
    }

    @Test
    public void toCookieStringOldWithMaxAgeMoreThanZero() throws Exception {
        Cookie cookie = new Cookie("test", "test");
        cookie.setComment("comment");
        cookie.setDomain(".bar.foo");
        cookie.setPath("/test");
        cookie.setMaxAge(100);
        cookie.setSecure(true);
        cookie.setVersion(0);
        cookie.setHttpOnly(true);
        long l = System.currentTimeMillis();
        String excepted = "test=test; Expires=" + OLD_COOKIE_DATE_FORMAT.format(new Date(l + 100 * 1000L)) + "; Domain=.bar.foo; Path=/test; Secure; HttpOnly";
        assertEquals(excepted, cookie.toCookieString(l));
    }

    @Test
    public void parseCookies() throws Exception {
        String toParse = "Cookie: test=test; ast=gdassd; sgasdasdasdsad=twefsdf;";
        Cookie[] cookies = Cookie.parseCookies(toParse);
        assertEquals(new Cookie("test", "test"), cookies[0]);
        assertEquals(new Cookie("ast", "gdassd"), cookies[1]);
        assertEquals(new Cookie("sgasdasdasdsad", "twefsdf"), cookies[2]);
    }
}