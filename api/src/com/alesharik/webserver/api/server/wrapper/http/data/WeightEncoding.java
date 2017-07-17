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

import com.alesharik.webserver.api.Utils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
public class WeightEncoding implements Weight<Encoding> {
    protected final float weight;
    protected final Encoding encoding;

    public WeightEncoding(Encoding encoding) {
        this(encoding, 1.0F);
    }

    public WeightEncoding(Encoding encoding, float weight) {
        this.weight = weight;
        this.encoding = encoding;
    }

    public String toHeaderString() {
        return encoding.getName() + (weight == 1.0F ? "" : ";q=" + weight);
    }

    public static WeightEncoding parseEncoding(String s) {
        int semicolonIndex = s.indexOf(';');
        if(semicolonIndex == -1)
            return new WeightEncoding(Encoding.parseEncoding(s));
        else {
            String[] divide = Utils.divideStringUnsafe(s, semicolonIndex, 3);
            return new WeightEncoding(Encoding.parseEncoding(divide[0]), Float.parseFloat(divide[1]));
        }
    }
}
