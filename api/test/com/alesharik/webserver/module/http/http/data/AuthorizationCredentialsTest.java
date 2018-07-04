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

import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AuthorizationCredentialsTest {
    @Test
    public void testParseNull() throws Exception {
        assertNull(Authorization.Credentials.parseAuthorizationCredentials("dGVzdHRlc3Q="));
    }

    @Test
    public void testParseNonNull() throws Exception {
        Authorization.Credentials credentials = Authorization.Credentials.parseAuthorizationCredentials("dGVzdDp0ZXN0");
        assertEquals("test", credentials.getLogin());
        assertEquals("test", credentials.getPassword());
    }

    @Test
    public void parseNonStandardCharset() throws Exception {
        Authorization.Credentials credentials = Authorization.Credentials.parseAuthorizationCredentials("YXNkOnNkZg==", Charset.forName("windows-1252"));
        assertEquals("asd", credentials.getLogin());
        assertEquals("sdf", credentials.getPassword());
    }
}