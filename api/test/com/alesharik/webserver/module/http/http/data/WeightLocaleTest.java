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

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class WeightLocaleTest {
    @Test
    public void toHeaderString() throws Exception {
        Locale locale = new Locale("en", "US");
        WeightLocale weightLocale = new WeightLocale(locale);
        assertEquals("en-US", weightLocale.toHeaderString());

        Locale locale1 = new Locale("en", "US");
        WeightLocale weightLocale1 = new WeightLocale(locale1, 0.5F);
        assertEquals("en-US;q=0.5", weightLocale1.toHeaderString());
    }

    @Test
    public void parseLocale() throws Exception {
        WeightLocale weightLocale = WeightLocale.parseLocale("en-US");

        assertEquals("en", weightLocale.getLocale().getLanguage());
        assertEquals("US", weightLocale.getLocale().getCountry());
        assertEquals(1.0F, weightLocale.getWeight(), 0.000005F);

        WeightLocale weightLocale1 = WeightLocale.parseLocale("en-US;q=0.5");
        assertEquals("en", weightLocale1.getLocale().getLanguage());
        assertEquals("US", weightLocale1.getLocale().getCountry());
        assertEquals(0.5F, weightLocale1.getWeight(), 0.000005F);
    }

    @Test
    public void parseAnyLocal() throws Exception {
        WeightLocale locale = WeightLocale.parseLocale("*");
        assertEquals(WeightLocale.ANY_LOCALE, locale.getLocale());

        WeightLocale locale1 = WeightLocale.parseLocale("*;q=0.5");
        assertEquals(WeightLocale.ANY_LOCALE, locale1.getLocale());
        assertEquals(0.5F, locale1.getWeight(), 0.000005);
    }
}