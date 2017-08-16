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
@Getter
@ToString
@EqualsAndHashCode
public class ContentRange {
    private static final Pattern pattern = Pattern.compile("[ /]");
    protected final AcceptRangesHeader.RangeType rangeType;
    protected final long start;
    protected final long end;
    protected final long size;

    public ContentRange(AcceptRangesHeader.RangeType rangeType, long size) {
        this.rangeType = rangeType;
        this.size = size;
        this.start = -1;
        this.end = -1;
    }

    public ContentRange(AcceptRangesHeader.RangeType rangeType, long start, long end) {
        this.rangeType = rangeType;
        this.start = start;
        this.end = end;
        this.size = -1;
    }

    public boolean hasRange() {
        return start != -1 && end != -1;
    }

    public boolean hasSize() {
        return size != -1;
    }

    public String toHeaderString() {
        return rangeType.getName() + ' ' + (hasRange() ? Long.toString(start) + '-' + end : '*') + '/' + (hasSize() ? Long.toString(size) : '*');
    }

    public static ContentRange fromHeaderString(String s) {
        String[] parts = pattern.split(s);
        if(parts.length < 3)
            throw new IllegalArgumentException();
        AcceptRangesHeader.RangeType type = AcceptRangesHeader.RangeType.parseRange(parts[0]);
        long start = -1;
        long end = -1;
        if(!parts[1].equals("*")) {
            String[] parts1 = parts[1].split("-");
            start = Long.parseLong(parts1[0]);
            end = Long.parseLong(parts1[1]);
        }
        long size = (parts[2].equals("*") ? -1 : Long.parseLong(parts[2]));
        return new ContentRange(type, start, end, size);
    }
}
