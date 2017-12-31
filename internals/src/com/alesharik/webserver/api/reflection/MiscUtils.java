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

package com.alesharik.webserver.api.reflection;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MiscUtils {
    public static String sliceString(String[] arr, int count, String separator) {
        if(count > arr.length)
            throw new ArrayIndexOutOfBoundsException();
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < count; i++) {
            if(i > 0)
                stringBuilder.append(separator);
            stringBuilder.append(arr[i]);
        }
        return stringBuilder.toString();
    }
}
