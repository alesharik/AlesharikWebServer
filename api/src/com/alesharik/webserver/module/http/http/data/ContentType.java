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

import com.alesharik.webserver.module.http.http.Utils;
import lombok.Getter;

import java.nio.charset.Charset;

@Getter
public class ContentType {
    private final MimeType type;
    private Charset charset;
    private String boundary;

    public ContentType(MimeType type, Charset charset) {
        this.type = type;
        this.charset = charset;
    }

    public ContentType(MimeType type) {
        this.type = type;
    }

    public ContentType(MimeType type, String boundary) {
        if(!type.toMimeType().equals("multipart/form-data"))
            throw new IllegalArgumentException();
        this.type = type;
        this.boundary = boundary;
    }

    public boolean hasBoundary() {
        return type.toMimeType().equals("multipart/form-data");
    }

    public String toHeaderString() {
        return hasBoundary() ? type.toMimeType() + "; boundary=" + boundary : type.toMimeType() + (charset == null ? null : "; charset=" + charset.toString());
    }

    public static ContentType parse(String s) {
        String[] parts = Utils.divideStringUnsafe(s, s.indexOf(';'), 2);
        MimeType type = MimeType.parseType(parts[0]);
        if(parts.length == 1)
            return new ContentType(type);
        else if(type.toMimeType().equals("multipart/form-data"))
            return new ContentType(type, parts[1].substring("boundary=".length()));
        else
            return new ContentType(type, parts[1].substring("charset=".length()));
    }
}
