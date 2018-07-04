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

import org.junit.Test;

import java.util.UUID;

import static com.alesharik.webserver.test.http.HttpMockUtils.request;
import static org.junit.Assert.assertTrue;

public class ConstantValidatorTest {
    @Test
    public void isTrue() {
        ConstantValidator validator = new ConstantValidator(true);

        for(int i = 0; i < 100; i++) {
            assertTrue(validator.isRequestValid(request()
                    .withRawUri("/" + UUID.randomUUID().toString())
                    .build()));
        }
    }

    @Test
    public void isFalse() {
        ConstantValidator validator = new ConstantValidator(false);

        for(int i = 0; i < 100; i++) {
            assertTrue(validator.isRequestValid(request()
                    .withRawUri("/" + UUID.randomUUID().toString())
                    .build()));
        }
    }
}