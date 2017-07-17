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

import com.alesharik.webserver.api.server.wrapper.http.data.WeightLocale;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class AcceptLanguageHeaderTest {
    private AcceptLanguageHeader header;

    @Before
    public void setUp() throws Exception {
        header = new AcceptLanguageHeader();
    }

    @Test
    public void getValueTest() throws Exception {
        WeightLocale[] locales = header.getValue("Accept-Language: fr-CH, en;q=0.9, *;q=0.5");

        assertEquals(3, locales.length);

        assertEquals("fr", locales[0].getLocale().getLanguage());
        assertEquals("CH", locales[0].getLocale().getCountry());
        assertEquals(1.0, locales[0].getWeight(), 0.00005);

        assertEquals("en", locales[1].getLocale().getLanguage());
        assertEquals(0.9, locales[1].getWeight(), 0.00005);

        assertEquals(WeightLocale.ANY_LOCALE, locales[2].getLocale());
        assertEquals(0.5, locales[2].getWeight(), 0.00005);
    }

    @Test
    public void buildTest() throws Exception {
        WeightLocale[] locales = new WeightLocale[]{
                new WeightLocale(new Locale("fr", "CH")),
                new WeightLocale(new Locale("en"), 0.9F),
                new WeightLocale(WeightLocale.ANY_LOCALE, 0.5F)
        };
        assertEquals("Accept-Language: fr-CH, en;q=0.9, *;q=0.5", header.build(locales));
    }
}