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

import com.alesharik.webserver.api.Utils;
import com.alesharik.webserver.api.server.wrapper.http.Header;
import com.alesharik.webserver.api.server.wrapper.http.data.WeightCharset;
import sun.misc.FloatingDecimal;

import javax.annotation.Nonnull;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.regex.Pattern;

public final class AcceptCharsetHeader extends Header<WeightCharset[]> {
    private final Pattern pattern;

    public AcceptCharsetHeader() {
        super("Accept-Charset");
        pattern = Pattern.compile(name + ": ");
    }

    /**
     * Ignore all unsupported charsets
     *
     * @param str header line
     */
    @Override
    public WeightCharset[] getValue(String str) {
        String charsets = pattern.matcher(str).replaceFirst("");

        String[] parts = charsets.split(", ");
        int partsLength = parts.length;
        WeightCharset[] ret = new WeightCharset[partsLength];
        int j = 0;

        for(int i = 0; i < partsLength; i++) {
            String part = parts[i];
            String quality = null;

            int semicolonIndex = part.indexOf(";");
            if(semicolonIndex != -1) {
                String[] divided = Utils.divideStringUnsafe(part, semicolonIndex, 1);
                quality = divided[1];
                part = divided[0];
            }

            Charset charset;
            if(part.equals("*")) {
                charset = null;
            } else {
                if(!Charset.isSupported(part))
                    continue;
                charset = Charset.forName(part);
            }


            if(quality == null)
                ret[j] = new WeightCharset(charset);
            else {
                float q = FloatingDecimal.parseFloat(Utils.divideStringUnsafe(quality, 2, 0)[1]);
                ret[j] = new WeightCharset(charset, q);
            }
            j++;
        }

        if(partsLength != j) {
            ret = Arrays.copyOf(ret, j);
        }
        return ret;
    }

    @Override
    public String build(@Nonnull WeightCharset[] value) {
        StringBuilder stringBuilder = new StringBuilder("Accept-Charset: ");
        for(int i = 0; i < value.length; i++) {
            WeightCharset val = value[i];

            if(i > 0)
                stringBuilder.append(", ");

            if(WeightCharset.isAnyCharset(val)) {
                stringBuilder.append('*');
            } else {
                stringBuilder.append(val.getCharset().toString().toLowerCase());
            }

            if(val.getWeight() != 1) {
                stringBuilder.append(";q=");
                stringBuilder.append(val.getWeight());
            }
        }
        return stringBuilder.toString();
    }
}
