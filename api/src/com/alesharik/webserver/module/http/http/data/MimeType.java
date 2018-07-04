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

import com.alesharik.webserver.api.Utils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * This class wrap web MIME type. '*' means any
 */
@EqualsAndHashCode
@ToString
@Getter
public class MimeType {
    protected final String type;
    protected final String subType;

    public MimeType(String type, String subType) {
        this.type = type;
        this.subType = subType;
    }

    /**
     * Return true if this type is AnyType(<code>&#42;&#47;&#42;</code>)
     */
    public boolean isAnyType() {
        return "*".equals(type) && "*".equals(subType);
    }

    /**
     * Return true if this type is AnySubtype(<code>type&#47;&#42;</code>)
     */
    public boolean isAnySubType() {
        return "*".equals(subType);
    }

    public static MimeType parseType(String str) {
        int slashPos = str.indexOf('/');
        if(slashPos == -1)
            throw new IllegalArgumentException(str + " is not a MIME type!");
        String[] parts = Utils.divideStringUnsafe(str, slashPos, 1);
        return new MimeType(parts[0], parts[1]);
    }

    public String toMimeType() {
        return type + '/' + subType;
    }
}
