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

package com.alesharik.webserver.base.exception;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class DevErrorTest {
    @Test
    public void render() {
        DevError devError = new DevError("wat", "wat wat wat", DevErrorTest.class);
        List<String> render = devError.renderMessage();
        assertEquals("====================DEV ERROR====================", render.get(0));
        assertEquals("CRITICAL ERROR DETECTED! FILE A BUG REPORT TO THE DEVELOPERS!", render.get(1));
        assertEquals("====================ERROR====================", render.get(2));
        assertEquals("wat", render.get(3));
        assertEquals("====================DESCRIPTION====================", render.get(4));
        assertEquals("wat wat wat", render.get(5));
        assertEquals("====================CLASS====================", render.get(6));
        assertEquals(DevErrorTest.class.getCanonicalName(), render.get(7));
        assertEquals("====================ERROR END====================", render.get(8));
    }

    @Test
    public void getMessage() {
        DevError devError = new DevError("wat", "wat wat wat", DevErrorTest.class);
        StringBuilder stringBuilder = new StringBuilder();
        devError.renderMessage().forEach(s -> {
            stringBuilder.append(s);
            stringBuilder.append('\n');
        });
        assertEquals(stringBuilder.toString(), devError.getMessage());
    }
}