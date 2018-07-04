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

import com.alesharik.webserver.module.http.http.header.ListHeader;

public enum Method {
    GET,
    POST,
    HEAD,
    OPTIONS,
    PUT,
    PATCH,
    DELETE,
    TRACE,
    CONNECT;

    static final class Factory implements ListHeader.Factory<Method> {

        @Override
        public Method newInstance(String value) {
            return Method.valueOf(value);
        }

        @Override
        public String toString(Method method) {
            return method.name();
        }

        @Override
        public Method[] newArray(int size) {
            return new Method[size];
        }
    }
}
