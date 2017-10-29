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

package com.alesharik.webserver.api.utils.classloader;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ClassAlreadyExistsExceptionTest {
    private ClassAlreadyExistsException exception;

    @Before
    public void setUp() throws Exception {
        exception = new ClassAlreadyExistsException("test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createClassWthEmptyClassName() throws Exception {
        //noinspection ThrowableNotThrown
        new ClassAlreadyExistsException("");
    }

    @Test
    public void testGetClassName() throws Exception {
        assertEquals("test", exception.getClassName());
    }

    @Test
    public void testGetMessage() throws Exception {
        assertEquals("Class test already exists!", exception.getMessage());
    }

    @Test
    public void testCauseMethods() throws Exception {
        assertNull(exception.getCause());
        assertNull(exception.initCause(new Exception()));
        assertNull(exception.getCause());
    }
}