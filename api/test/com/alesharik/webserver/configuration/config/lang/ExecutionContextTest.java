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

package com.alesharik.webserver.configuration.config.lang;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ExecutionContextTest {
    @Test
    public void parseTest() {
        assertEquals(ExecutionContext.CALL, ExecutionContext.parse(null));
        assertEquals(ExecutionContext.GLOBAL, ExecutionContext.parse("global"));
        assertEquals(ExecutionContext.MODULE, ExecutionContext.parse("module"));
        assertEquals(ExecutionContext.CALL, ExecutionContext.parse("call"));
        assertEquals(ExecutionContext.GLOBAL, ExecutionContext.parse("GLOBAL"));
        assertEquals(ExecutionContext.MODULE, ExecutionContext.parse("MODULE"));
        assertEquals(ExecutionContext.CALL, ExecutionContext.parse("CALL"));
        assertEquals(ExecutionContext.GLOBAL, ExecutionContext.parse("glObAl"));
        assertEquals(ExecutionContext.MODULE, ExecutionContext.parse("MOduLe"));
        assertEquals(ExecutionContext.CALL, ExecutionContext.parse("caLL"));
        assertNull(ExecutionContext.parse("asd"));
        assertNull(ExecutionContext.parse("cal"));
    }
}