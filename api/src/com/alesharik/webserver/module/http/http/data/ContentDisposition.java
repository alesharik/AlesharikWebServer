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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class ContentDisposition {
    protected final Type type;
    @Nullable
    protected final String name;
    @Nullable
    protected final String fileName;

    @Nullable
    @Setter
    protected ContentType contentType;

    public ContentDisposition(@Nonnull Type type, String name) {
        this(type, name, null);
    }

    public ContentDisposition(@Nonnull Type type) {
        this(type, null, null);
    }

    public static ContentDisposition parse(String line) {
        String[] parts = line.split(";");
        if(parts.length < 1)
            throw new IllegalArgumentException();
        Type type = Type.parse(parts[0]);
        String name = null;
        String fileName = null;
        String fileNameAsterisk = null;
        if(parts.length > 1)
            for(int i = 1; i < parts.length; i++) {
                String[] trim = parts[i].trim().split("=", 2);
                if(trim[0].startsWith("name"))
                    name = trim[1];
                else if(trim[0].equals("filename"))
                    fileName = trim[1];
                else if(trim[0].equals("filename*"))
                    fileNameAsterisk = trim[1];
            }

        if(fileNameAsterisk != null)
            fileName = fileNameAsterisk;
        return new ContentDisposition(type, name, fileName);
    }

    public String toHeaderString() {
        return type.toString() + (name != null ? ("; name=" + name) : "") + (fileName != null ? ("; filename=" + fileName) : "");
    }

    public enum Type {
        INLINE,
        ATTACHMENT,
        FORM_DATA;

        public static Type parse(String s) {
            switch (s) {
                case "inline":
                    return INLINE;
                case "attachment":
                    return ATTACHMENT;
                case "form-data":
                    return FORM_DATA;
                default:
                    throw new IllegalArgumentException("ContentDisposition type " + s + " not found!");
            }
        }

        @Override
        public String toString() {
            switch (this) {
                case INLINE:
                    return "inline";
                case ATTACHMENT:
                    return "attachment";
                case FORM_DATA:
                    return "form-data";
                default:
                    throw new IllegalStateException();
            }
        }
    }
}
