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

package com.alesharik.webserver.module.http.util;

import com.alesharik.webserver.module.http.http.Request;
import com.alesharik.webserver.module.http.http.data.MimeType;


//TODO refactor
public interface FileContentProvider {
    boolean hasFile(Request request);

    String getName(Request request);

    long getLength(Request request);

    /**
     * @return byte[] with 0 length then can't provide this range, overwise data
     */
    byte[] getRangedData(Request request, long start, long size);

    MimeType getMimeType(Request request);
}
