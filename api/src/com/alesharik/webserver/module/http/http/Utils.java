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

package com.alesharik.webserver.module.http.http;

import com.alesharik.webserver.api.documentation.PrivateApi;
import lombok.experimental.UtilityClass;

@PrivateApi
@UtilityClass
public class Utils {
    public static String[] divideStringUnsafe(String s, int divide, int space) {
        if(s.length() <= (divide + space))
            return new String[] {s};
        else
            return new String[] {s.substring(0, divide), s.substring(divide + space)};
    }
}
