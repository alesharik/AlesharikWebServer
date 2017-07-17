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

import com.alesharik.webserver.api.server.wrapper.http.HeaderManager;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class WarningTest {
    @Test
    public void hasDate() throws Exception {
        Warning warning = new Warning(Warning.Code.CODE_110, "0", "");
        assertFalse(warning.hasDate());

        Warning warning1 = new Warning(Warning.Code.CODE_110, "", "", new Date());
        assertTrue(warning1.hasDate());
    }

    @Test
    public void toHeaderString() throws Exception {
        Warning warning = new Warning(Warning.Code.CODE_110, "tes", "test");
        assertEquals("110 tes \"test\"", warning.toHeaderString());

        Date date = new Date(System.currentTimeMillis());
        Warning warning1 = new Warning(Warning.Code.CODE_110, "tes", "test", date);
        assertEquals("110 tes \"test\" \"" + HeaderManager.WEB_DATE_FORMAT.get().format(date) + '\"', warning1.toHeaderString());
    }

    @Test
    public void parseTest() throws Exception {
        Warning warning = Warning.parse("110 tes \"test\"");
        assertEquals(Warning.Code.CODE_110, warning.getCode());
        assertEquals("tes", warning.getAgent());
        assertEquals("test", warning.getText());
        assertFalse(warning.hasDate());

        Date date = new Date(1000);
        Warning warning1 = Warning.parse("110 tes \"test\" \"" + HeaderManager.WEB_DATE_FORMAT.get().format(date) + '\"');
        assertEquals(Warning.Code.CODE_110, warning1.getCode());
        assertEquals("tes", warning1.getAgent());
        assertEquals("test", warning1.getText());
        assertTrue(warning1.hasDate());
        assertEquals(date, warning1.getDate());
    }

    @Test
    public void codeTest() throws Exception {
        for(Warning.Code code : Warning.Code.values()) {
            assertEquals(code, Warning.Code.forCode(code.getCode()));
        }
    }
}