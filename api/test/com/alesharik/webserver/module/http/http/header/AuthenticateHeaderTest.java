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

package com.alesharik.webserver.module.http.http.header;

import com.alesharik.webserver.module.http.http.data.Authentication;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AuthenticateHeaderTest {
    private AuthenticateHeader header;

    @Before
    public void setUp() throws Exception {
        header = new AuthenticateHeader("WWW-Authenticate");
    }

    @Test
    public void getValue() throws Exception {
        Authentication authentication = header.getValue("WWW-Authenticate: Basic realm=\"Access to the staging site\"");
        assertEquals(Authentication.Type.BASIC, authentication.getType());
        assertTrue(authentication.hasRealm());
        assertEquals("Access to the staging site", authentication.getRealm());

        Authentication authentication1 = header.getValue("WWW-Authenticate: Basic");
        assertEquals(Authentication.Type.BASIC, authentication1.getType());
        assertFalse(authentication1.hasRealm());
    }

    @Test
    public void build() throws Exception {
        assertEquals("WWW-Authenticate: Basic", header.build(new Authentication(Authentication.Type.BASIC)));
        assertEquals("WWW-Authenticate: Basic realm=\"Access to the staging site\"", header.build(new Authentication(Authentication.Type.BASIC, "Access to the staging site")));
    }

}