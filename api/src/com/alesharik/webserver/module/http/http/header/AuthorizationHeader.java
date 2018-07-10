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

import com.alesharik.webserver.module.http.http.Header;
import com.alesharik.webserver.module.http.http.Utils;
import com.alesharik.webserver.module.http.http.data.Authentication;
import com.alesharik.webserver.module.http.http.data.Authorization;

public class AuthorizationHeader extends Header<Authorization> {

    public AuthorizationHeader(String name) {
        super(name);
    }

    @Override
    public Authorization getValue(String str) {
        String s = pattern.matcher(str).replaceFirst("");
        int pos = s.indexOf(' ');
        if(pos == -1) {
            pos = s.indexOf(160);
            if(pos == -1)
                return null;
        }
        String[] divide = Utils.divideStringUnsafe(s, pos, 1);
        return new Authorization(Authentication.Type.parse(divide[0]), divide[1]);
    }

    @Override
    public String build(Authorization value) {
        return "Authorization: " + value.getType().getName() + ' ' + value.getCredentials();
    }
}
