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
import lombok.Getter;
import lombok.ToString;

import java.util.regex.Pattern;

public final class AcceptRangesHeader extends Header<AcceptRangesHeader.Range> {
    private final Pattern pattern;

    public AcceptRangesHeader() {
        super("Accept-Ranges");
        pattern = Pattern.compile(name + ": ");
    }

    /**
     * Default value is {@link Range#NONE}
     *
     * @param str header line
     */
    @Override
    public Range getValue(String str) {
        String body = pattern.matcher(str).replaceFirst("");
        return Range.parseRange(body);
    }

    @Override
    public String build(Range value) {
        return "Accept-Ranges: " + value.getName();
    }

    @ToString
    public enum Range {
        BYTES("bytes"),
        NONE("none");

        @Getter
        private final String name;

        Range(String name) {
            this.name = name;
        }

        public static Range parseRange(String str) {
            if(str.equals("bytes"))
                return BYTES;
            return NONE;
        }
    }
}
