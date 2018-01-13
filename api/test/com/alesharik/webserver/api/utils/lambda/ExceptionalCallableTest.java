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

package com.alesharik.webserver.api.utils.lambda;

import org.junit.Test;

import static com.alesharik.webserver.api.utils.lambda.LambdaUtils.exceptional;
import static org.junit.Assert.*;

public class ExceptionalCallableTest {
    @Test
    public void okCall() {
        Object o = new Object();
        Object result = exceptional(() -> o).call();

        assertSame(o, result);
    }

    @Test
    public void exceptionCall() {
        Object o =
                exceptional(() -> {
                    throw new IllegalMonitorStateException();
                })
                        .onError(e -> assertTrue(e instanceof IllegalMonitorStateException))
                        .call();
        assertNull(o);
    }
}