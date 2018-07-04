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
import com.alesharik.webserver.module.http.http.data.WeightLocale;

public final class AcceptLanguageHeader extends Header<WeightLocale[]> {

    public AcceptLanguageHeader() {
        super("Accept-Language");
    }

    @Override
    public WeightLocale[] getValue(String str) {
        String locales = pattern.matcher(str).replaceFirst("");
        String[] parts = locales.split(", ");
        WeightLocale[] weightEncodings = new WeightLocale[parts.length];
        for(int i = 0; i < parts.length; i++) {
            weightEncodings[i] = WeightLocale.parseLocale(parts[i]);
        }
        return weightEncodings;
    }

    @Override
    public String build(WeightLocale[] value) {
        StringBuilder stringBuilder = new StringBuilder("Accept-Language: ");
        int i = 0;
        for(WeightLocale weightLocale : value) {
            if(i != 0)
                stringBuilder.append(", ");
            stringBuilder.append(weightLocale.toHeaderString());
            i++;
        }

        return stringBuilder.toString();
    }
}
