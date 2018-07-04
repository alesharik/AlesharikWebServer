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

import com.alesharik.webserver.api.Utils;
import com.alesharik.webserver.module.http.http.Header;
import com.alesharik.webserver.module.http.http.data.Authentication;

public final class AuthenticateHeader extends Header<Authentication> {

    public AuthenticateHeader(String name) {
        super(name);
    }

    @Override
    public Authentication getValue(String str) {
        String s = pattern.matcher(str).replaceFirst("");
        int spaceIndex = s.indexOf(' ');
        if(spaceIndex == -1) {
            return new Authentication(Authentication.Type.parse(s));
        }
        String[] divide = Utils.divideStringUnsafe(s, spaceIndex, 8); //` realm="` - 8 chars
        return new Authentication(Authentication.Type.parse(divide[0]), divide[1].substring(0, divide[1].length() - 1));
    }

    @Override
    public String build(Authentication value) {
        if(value.hasRealm())
            return name + ": " + value.getType().getName() + " realm=\"" + value.getRealm() + "\"";
        else
            return name + ": " + value.getType().getName();
    }
}
