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

package com.alesharik.webserver.module.http.bundle.impl.validator;

import com.alesharik.webserver.module.http.bundle.Validator;
import org.junit.Test;

import static com.alesharik.webserver.test.http.HttpMockUtils.request;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StartsWithValidatorTest {
    @Test
    public void test() {
        Validator validator = new StartsWithValidator("/test");

        assertTrue(validator.isRequestValid(request()
                .withRawUri("/test/a")
                .build()));
        assertTrue(validator.isRequestValid(request()
                .withRawUri("/test/b/a")
                .build()));
        assertTrue(validator.isRequestValid(request()
                .withRawUri("/test/b/a/q")
                .build()));
        assertFalse(validator.isRequestValid(request()
                .withRawUri("/tes/a")
                .build()));
        assertFalse(validator.isRequestValid(request()
                .withRawUri("/tet/b/a")
                .build()));
        assertFalse(validator.isRequestValid(request()
                .withRawUri("/tst/b/a/q")
                .build()));
    }
}