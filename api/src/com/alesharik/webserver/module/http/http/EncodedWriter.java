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

import lombok.AccessLevel;
import lombok.Setter;

import java.nio.charset.Charset;

public class EncodedWriter {
    protected final OutputBuffer buffer;
    @Setter(AccessLevel.PACKAGE)
    protected volatile Charset charset;

    public EncodedWriter(OutputBuffer buffer, Charset charset) {
        this.buffer = buffer;
        this.charset = charset;
    }

    public void write(String s) {
        buffer.write(s.getBytes(charset));
    }
}
