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

import lombok.Getter;
import lombok.ToString;

/**
 * Encoding for <code>Accept-Encoding</code> header. All enum comments was taken from <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Encoding">HERE</a>
 */
@ToString
public enum Encoding {
    /**
     * A compression format using the Lempel-Ziv coding (LZ77), with a 32-bit CRC.
     */
    GZIP("gzip"),
    /**
     * A compression format using the Lempel-Ziv-Welch (LZW) algorithm.
     */
    COMPRESS("compress"),
    /**
     * A compression format using the zlib structure, with the deflate compression algorithm.
     */
    DEFLATE("deflate"),
    /**
     * A compression format using the Brotli algorithm.
     */
    BR("br"),
    /**
     * Indicates the identity function (i.e. no compression, nor modification). This value is always considered as acceptable, even if not present.
     */
    IDENTITY("identity"),
    /**
     * Matches any content encoding not already listed in the header. This is the default value if the header is not present. It doesn't mean that any algorithm is supported; merely that no preference is expressed.
     */
    ALL("*");

    @Getter
    private final String name;

    Encoding(String name) {
        this.name = name;
    }

    /**
     * Find encoding for name
     *
     * @param enc encoding name from header
     * @return parsed encoding or ALL
     */
    public static Encoding parseEncoding(String enc) {
        for(Encoding encoding : values()) {
            if(encoding.name.equals(enc))
                return encoding;
        }
        return ALL;
    }
}
