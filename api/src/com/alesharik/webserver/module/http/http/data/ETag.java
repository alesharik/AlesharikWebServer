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

import lombok.Getter;

@Getter
public class ETag {
    public static final ETag ANY_TAG = new ETag("*", false);
    protected final String tag;
    protected final boolean weak;

    public ETag(String tag, boolean weak) {
        this.tag = tag;
        this.weak = weak;
    }

    public String toHeaderString() {
        return (weak ? "W/" : "") + '\"' + tag + '\"';
    }

    public static ETag parse(String s) {
        if(s.startsWith("W/"))
            return new ETag(s.substring(3, s.length() - 1), true);
        else
            return new ETag(s.substring(1, s.length() - 1), false);
    }
}
