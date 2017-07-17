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

package com.alesharik.webserver.api.server.wrapper.http.header;

import com.alesharik.webserver.api.server.wrapper.http.data.Authentication;
import com.alesharik.webserver.api.server.wrapper.http.data.Authorization;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AuthorizationHeaderTest {
    private AuthorizationHeader header;

    @Before
    public void setUp() throws Exception {
        header = new AuthorizationHeader("Authorization");
    }

    @Test
    public void getValue() throws Exception {
        Authorization authorization = header.getValue("Authorization: Basic YWxhZGRpbjpvcGVuc2VzYW1l");
        assertEquals(Authentication.Type.BASIC, authorization.getType());
        assertEquals("YWxhZGRpbjpvcGVuc2VzYW1l", authorization.getCredentials());
    }

    @Test
    public void build() throws Exception {
        Authorization authorization = new Authorization(Authentication.Type.BASIC, "YWxhZGRpbjpvcGVuc2VzYW1l");
        assertEquals("Authorization: Basic YWxhZGRpbjpvcGVuc2VzYW1l", header.build(authorization));
    }
}