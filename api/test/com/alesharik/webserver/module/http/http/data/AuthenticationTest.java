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

import static org.junit.Assert.*;

public class AuthenticationTest {
    @Test
    public void hasRealm() throws Exception {
        Authentication authentication = new Authentication(Authentication.Type.BEARER, "asd");
        assertTrue(authentication.hasRealm());


        Authentication authentication1 = new Authentication(Authentication.Type.BEARER);
        assertFalse(authentication1.hasRealm());
    }

    @Test
    public void testParse() throws Exception {
        for(Authentication.Type type : Authentication.Type.values()) {
            assertEquals(type, Authentication.Type.parse(type.getName()));
        }
        assertNull(Authentication.Type.parse("asd"));
    }
}