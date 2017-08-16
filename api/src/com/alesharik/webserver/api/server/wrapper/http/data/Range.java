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

package com.alesharik.webserver.api.server.wrapper.http.data;

import com.alesharik.webserver.api.server.wrapper.http.header.AcceptRangesHeader;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.regex.Pattern;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
public class Range {
    private static final Pattern pattern = Pattern.compile("-");
    protected final long start;
    protected final long end;
    protected final AcceptRangesHeader.RangeType rangeType;

    public Range(long start, AcceptRangesHeader.RangeType rangeType) {
        this.start = start;
        this.end = -1;
        this.rangeType = rangeType;
    }

    public boolean hasEnd() {
        return end != -1;
    }

    public String toHeaderString() {
        return Long.toString(start) + '-' + (hasEnd() ? Long.toString(end) : "");
    }

    public static Range parse(AcceptRangesHeader.RangeType type, String s) {
        String[] parts = pattern.split(s);
        if(parts.length == 1)
            return new Range(Long.parseLong(parts[0]), type);
        else
            return new Range(Long.parseLong(parts[0]), Long.parseLong(parts[1]), type);
    }
}
