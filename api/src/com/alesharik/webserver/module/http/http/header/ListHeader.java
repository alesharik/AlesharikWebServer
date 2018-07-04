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

public class ListHeader<T> extends Header<T[]> {
    private final Factory<T> factory;

    public ListHeader(String name, Factory<T> factory) {
        super(name);
        this.factory = factory;
    }

    @Override
    public T[] getValue(String str) {
        String body = pattern.matcher(str).replaceFirst("");
        String[] parts = body.split(", ");
        T[] ret = factory.newArray(parts.length);
        for(int i = 0; i < parts.length; i++) {
            ret[i] = factory.newInstance(parts[i]);
        }
        return ret;
    }

    @Override
    public String build(T[] value) {
        StringBuilder stringBuilder = new StringBuilder(name);
        stringBuilder.append(": ");
        int i = 0;
        for(T t : value) {
            if(i != 0)
                stringBuilder.append(", ");
            stringBuilder.append(factory.toString(t));
            i++;
        }
        return stringBuilder.toString();
    }

    public interface Factory<T> {
        T newInstance(String value);

        String toString(T t);

        T[] newArray(int size);
    }
}
