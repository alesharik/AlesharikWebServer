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
import com.alesharik.webserver.module.http.http.data.ETag;

public final class IfETagHeader extends Header<ETag[]> {
    public IfETagHeader(String name) {
        super(name);
    }

    @Override
    public ETag[] getValue(String str) {
        String s = pattern.matcher(str).replaceFirst("");
        String[] strings = s.split(", ");
        ETag[] tags = new ETag[strings.length];
        for(int i = 0; i < tags.length; i++) {
            tags[i] = "*".equals(strings[i]) ? ETag.ANY_TAG : ETag.parse(strings[i]);
        }
        return tags;
    }

    @Override
    public String build(ETag[] value) {
        StringBuilder stringBuilder = new StringBuilder(name + ": ");
        boolean notFirst = false;
        for(ETag eTag : value) {
            if(notFirst)
                stringBuilder.append(", ");
            else
                notFirst = true;
            stringBuilder.append(eTag == ETag.ANY_TAG ? "*" : eTag.toHeaderString());
        }
        return stringBuilder.toString();
    }
}
