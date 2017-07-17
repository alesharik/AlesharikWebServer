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

package com.alesharik.webserver.api.server.wrapper.http;

import lombok.Getter;

import javax.annotation.Nullable;

public enum HttpVersion {
    HTTP_0_9("HTTP/0.9"),
    HTTP_1_0("HTTP/1.0"),
    HTTP_1_1("HTTP/1.1"),
    HTTP_2("HTTP/2");

    @Getter
    private final String value;

    HttpVersion(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Nullable
    public static HttpVersion getValue(String str) {
        for(HttpVersion httpVersion : values()) {
            if(httpVersion.value.equals(str))
                return httpVersion;
        }
        return null;
    }
}
