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

import lombok.Getter;

import java.util.regex.Pattern;

/**
 * All headers are preferred to be Singleton
 *
 * @param <T>
 */
public abstract class Header<T> {
    @Getter
    protected final String name;
    protected final Pattern pattern;

    protected Header(String name) {
        this.name = name;
        this.pattern = Pattern.compile("(?i:" + name + ": )", Pattern.CASE_INSENSITIVE);
    }

    /**
     * @param str header line
     * @return parsed value
     */
    public abstract T getValue(String str);

    public abstract String build(T value);
}
