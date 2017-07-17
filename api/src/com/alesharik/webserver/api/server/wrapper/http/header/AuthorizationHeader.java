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

import com.alesharik.webserver.api.Utils;
import com.alesharik.webserver.api.server.wrapper.http.Header;
import com.alesharik.webserver.api.server.wrapper.http.data.Authentication;
import com.alesharik.webserver.api.server.wrapper.http.data.Authorization;

import java.util.regex.Pattern;

public class AuthorizationHeader extends Header<Authorization> {
    private final Pattern pattern;

    public AuthorizationHeader(String name) {
        super(name);
        pattern = Pattern.compile(name + ": ");
    }

    @Override
    public Authorization getValue(String str) {
        String s = pattern.matcher(str).replaceFirst("");
        String[] divide = Utils.divideStringUnsafe(s, s.indexOf(' '), 1);
        return new Authorization(Authentication.Type.parse(divide[0]), divide[1]);
    }

    @Override
    public String build(Authorization value) {
        return "Authorization: " + value.getType().getName() + ' ' + value.getCredentials();
    }
}
