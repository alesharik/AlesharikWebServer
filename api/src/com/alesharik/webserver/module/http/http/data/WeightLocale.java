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

import com.alesharik.webserver.api.Utils;
import lombok.Getter;

import java.util.Locale;

@Getter
public class WeightLocale implements Weight<Locale> {
    public static final Locale ANY_LOCALE = new Locale("*");

    protected final float weight;
    protected final Locale locale;

    public WeightLocale(Locale locale) {
        this(locale, 1.0F);
    }

    public WeightLocale(Locale locale, float weight) {
        this.weight = weight;
        this.locale = locale;
    }

    public String toHeaderString() {
        return (locale == ANY_LOCALE ? "*" : locale.toLanguageTag()) + (weight == 1.0 ? "" : ";q=" + weight);
    }

    public static WeightLocale parseLocale(String s) {
        int semicolonIndex = s.indexOf(';');
        if(semicolonIndex == -1)
            return new WeightLocale(getLocale(s));
        else {
            String[] divided = Utils.divideStringUnsafe(s, semicolonIndex, 3);
            return new WeightLocale(getLocale(divided[0]), Float.parseFloat(divided[1]));
        }
    }

    private static Locale getLocale(String s) {
        return s.equals("*") ? ANY_LOCALE : Locale.forLanguageTag(s);
    }
}
