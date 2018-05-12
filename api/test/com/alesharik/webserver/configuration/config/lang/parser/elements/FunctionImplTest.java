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

public class FunctionImplTest {
    @Test
    public void equality() {
        FunctionImpl function = new FunctionImpl("a", "b");
        FunctionImpl function1 = new FunctionImpl("a", "b");

        FunctionImpl function2 = new FunctionImpl("a", "c");
        FunctionImpl function3 = new FunctionImpl("q", "b");

        assertEquals(function, function1);

        assertNotEquals(function, function2);
        assertNotEquals(function, function3);

        assertNotEquals(function1, function2);
        assertNotEquals(function1, function3);

        assertNotEquals(function2, function3);

        assertEquals(function.hashCode(), function1.hashCode());

        assertNotEquals(function.hashCode(), function2.hashCode());
        assertNotEquals(function.hashCode(), function3.hashCode());

        assertNotEquals(function1.hashCode(), function2.hashCode());
        assertNotEquals(function1.hashCode(), function3.hashCode());

        assertNotEquals(function2.hashCode(), function3.hashCode());
    }

    @Test
    public void stringify() {
        FunctionImpl function = new FunctionImpl("", "");

        assertNotNull(function.toString());

        FunctionImpl function1 = new FunctionImpl("a", "b");
        assertNotNull(function1.toString());
    }
}