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

package com.alesharik.webserver.api.functions;

import org.junit.Before;
import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.assertTrue;

public class TripleFunctionTest {
    private TripleFunction<Integer, Integer, Integer, Integer> functionA;
    private Function<Integer, Integer> functionB;

    @Before
    public void setUp() throws Exception {
        functionA = (integer, integer2, integer3) -> integer + integer2 + integer3;
        functionB = integer -> integer + 1;
    }

    @Test
    public void andThen() throws Exception {
        assertTrue((functionA.andThen(functionB).apply(1, 2, 3)) == 7);
    }

}