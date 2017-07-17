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

package com.alesharik.webserver.api.server.wrapper.http.header;

import com.alesharik.webserver.api.server.wrapper.http.Header;

import java.util.regex.Pattern;

public class ObjectHeader<T> extends Header<T> {
    private final Factory<T> factory;
    private final Pattern pattern;

    public ObjectHeader(String name, Factory<T> factory) {
        super(name);
        this.factory = factory;
        this.pattern = Pattern.compile(name + ": ");
    }

    @Override
    public T getValue(String str) {
        String body = pattern.matcher(str).replaceFirst("");
        return factory.newInstance(body);
    }

    @Override
    public String build(T value) {
        return name + ": " + factory.toString(value);
    }

    public interface Factory<T> {
        T newInstance(String s);

        String toString(T t);
    }
}
