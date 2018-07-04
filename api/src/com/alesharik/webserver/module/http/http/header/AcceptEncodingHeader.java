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
import com.alesharik.webserver.module.http.http.data.WeightEncoding;

public final class AcceptEncodingHeader extends Header<WeightEncoding[]> {

    public AcceptEncodingHeader() {
        super("Accept-Encoding");
    }

    /**
     * All bad encoding names replaced by {@link com.alesharik.webserver.module.http.http.data.Encoding#ALL}
     *
     * @param str header line
     */
    @Override
    public WeightEncoding[] getValue(String str) {
        String encodings = pattern.matcher(str).replaceFirst("");
        String[] parts = encodings.split(", ");
        WeightEncoding[] weightEncodings = new WeightEncoding[parts.length];
        for(int i = 0; i < parts.length; i++) {
            weightEncodings[i] = WeightEncoding.parseEncoding(parts[i]);
        }
        return weightEncodings;
    }

    @Override
    public String build(WeightEncoding[] value) {
        StringBuilder stringBuilder = new StringBuilder("Accept-Encoding: ");
        int i = 0;
        for(WeightEncoding weightEncoding : value) {
            if(i != 0)
                stringBuilder.append(", ");
            stringBuilder.append(weightEncoding.toHeaderString());
            i++;
        }

        return stringBuilder.toString();
    }
}
