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

package com.alesharik.webserver.configuration.config.lang.parser.elements;

import org.junit.Test;

import static org.junit.Assert.*;

public class CodeImplTest {
    @Test
    public void equality() {
        CodeImpl code = new CodeImpl("a", "b", "c");
        CodeImpl code1 = new CodeImpl("a", "b", "c");

        CodeImpl code2 = new CodeImpl("a", "q", "c");
        CodeImpl code3 = new CodeImpl("a", "b", "a");

        assertEquals(code, code1);
        assertNotEquals(code, code2);
        assertNotEquals(code, code3);
        assertNotEquals(code2, code3);
        assertNotEquals(code1, code2);
        assertNotEquals(code1, code3);

        assertEquals(code.hashCode(), code1.hashCode());
        assertNotEquals(code.hashCode(), code2.hashCode());
        assertNotEquals(code, code3.hashCode());
        assertNotEquals(code2.hashCode(), code3.hashCode());
        assertNotEquals(code1.hashCode(), code2.hashCode());
        assertNotEquals(code1.hashCode(), code3.hashCode());
    }

    @Test
    public void stringify() {
        CodeImpl code = new CodeImpl("", "", "");

        assertNotNull(code.toString());

        CodeImpl code1 = new CodeImpl("a", "b", "c");
        assertNotNull(code1.toString());
    }
}