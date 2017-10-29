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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class InstantiableClassLoaderTest {
    private InstantiableClassLoader classLoader;

    @Before
    public void setUp() throws Exception {
        classLoader = new InstantiableClassLoader(this.getClass().getClassLoader());
    }

    @Test
    public void createNormalInstance() throws Exception {
        Object o = classLoader.newInstance("java.lang.Object");
        assertNotNull(o);
    }

    @Test
    public void createNotExistingClass() throws Exception {
        Object o = classLoader.newInstance("java.wat.NotExists");
        assertNull(o);
    }

    @Test
    public void createClassWithNoConstructor() throws Exception {
        Test1 o = (Test1) classLoader.newInstance("com.alesharik.webserver.api.utils.classloader.InstantiableClassLoaderTest$Test1");
        assertNull(o);
    }

    @Test
    public void createClassWithPrivateConstructor() throws Exception {
        Test2 o = (Test2) classLoader.newInstance("com.alesharik.webserver.api.utils.classloader.InstantiableClassLoaderTest$Test2");
        assertNotNull(o);
    }

    @Test(expected = RuntimeException.class)
    public void createClassWithExceptionInConstructor() throws Exception {
        Test3 o = (Test3) classLoader.newInstance("com.alesharik.webserver.api.utils.classloader.InstantiableClassLoaderTest$Test3");
        assertNull(o);
    }

    @Test(expected = RuntimeException.class)
    public void createClassWithExceptionInPrivateConstructor() throws Exception {
        Test4 o = (Test4) classLoader.newInstance("com.alesharik.webserver.api.utils.classloader.InstantiableClassLoaderTest$Test4");
        assertNull(o);
    }

    private static final class Test1 {
        private Test1(@SuppressWarnings("unused") int ignored) {
        }
    }

    private static final class Test2 {
        private Test2() {
        }
    }

    static final class Test3 {
        public Test3() {
            throw new IllegalArgumentException("hi");
        }
    }

    private static final class Test4 {
        private Test4() {
            throw new IllegalArgumentException("hi");
        }
    }
}