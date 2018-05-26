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

package com.alesharik.webserver.api;

import com.alesharik.webserver.test.TestUtils;
import org.junit.Test;

public class TestUtilsTest {
    @Test(expected = AssertionError.class)
    public void nonFinal() {
        TestUtils.assertUtilityClass(NonFinal.class);
    }

    @Test(expected = AssertionError.class)
    public void withPublicConstructor() {
        TestUtils.assertUtilityClass(WithPublicConstructor.class);
    }

    @Test(expected = AssertionError.class)
    public void constructorWithArgs() {
        TestUtils.assertUtilityClass(CtorWithArgs.class);
    }

    @Test(expected = AssertionError.class)
    public void ctorWithZeroCtors() {
        TestUtils.assertUtilityClass(int.class);
    }

    @Test(expected = AssertionError.class)
    public void ctorWithIncorrectException() {
        TestUtils.assertUtilityClass(CtorWithIncorrectException.class);
    }

    @Test
    public void ok() {
        TestUtils.assertUtilityClass(CtorOK.class);
    }

    @Test
    public void assertUtility() {
        TestUtils.assertUtilityClass(TestUtils.class);
    }

    private static class NonFinal {
    }

    private static final class WithPublicConstructor {
        public WithPublicConstructor() {
        }
    }

    private static final class CtorWithArgs {
        private CtorWithArgs(@SuppressWarnings("unused") String a) {
        }
    }

    private static final class CtorWithIncorrectException {
        private CtorWithIncorrectException() {
            throw new RuntimeException();
        }
    }

    private static final class CtorOK {
        private CtorOK() {
            throw new UnsupportedOperationException();
        }
    }
}