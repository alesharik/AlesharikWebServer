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

package com.alesharik.webserver.api.reflection;

import com.alesharik.webserver.api.TestUtils;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReflectUtilsTest {
    @Test
    public void getMethods() throws Exception {
        List<Method> methods = ReflectUtils.getAllDeclaredMethods(Test5.class);
        assertTrue(methods.contains(C.class.getDeclaredMethod("a")));
        assertTrue(methods.contains(Test5.class.getDeclaredMethod("b")));
        assertTrue(methods.contains(Test4.class.getDeclaredMethod("c")));
        assertTrue(methods.contains(Test5.class.getDeclaredMethod("asd")));
        assertEquals(16, methods.size());
    }

    @Test
    public void getFields() throws Exception {
        List<Field> fields = ReflectUtils.getAllDeclaredFields(Test5.class);
        assertTrue(fields.contains(C.class.getDeclaredField("TEST")));
        assertTrue(fields.contains(Test3.class.getDeclaredField("a1")));
        assertTrue(fields.contains(Test4.class.getDeclaredField("a1")));
        assertTrue(fields.contains(Test5.class.getDeclaredField("a1")));
        assertTrue(fields.contains(Test5.class.getDeclaredField("b1")));
        assertEquals(5, fields.size());
    }

    @Test
    public void getInnerClasses() throws Exception {
        List<Class<?>> classes = ReflectUtils.getAllInnerClasses(Test5.class);
        assertTrue(classes.contains(C.I1.class));
        assertTrue(classes.contains(Test5.T2.class));
        assertTrue(classes.contains(Test4.T3.class));
        assertTrue(classes.contains(Test3.T3.class));
        assertEquals(4, classes.size());
    }

    @Test
    public void testUtils() throws Exception {
        TestUtils.assertUtilityClass(ReflectUtils.class);
    }

    private interface A {
        default void a() {
        }
    }

    private interface B {
        void b();
    }

    private interface C extends B, A {
        int TEST = 1;

        void c();

        @Override
        default void a() {

        }

        class I1 {

        }
    }

    private static final class Test5 extends Test4 {
        private final String a1 = "asd";
        private final String b1 = "asd";

        private void asd() {
        }

        @Override
        public void b() {
        }

        private class T2 {

        }
    }

    private abstract static class Test4 extends Test3 implements C {
        private final String a1 = "asdfasd";

        @Override
        public abstract void b();

        @Override
        public void c() {

        }

        private class T3 {

        }
    }

    private static class Test3 implements A {
        protected final String a1 = "asdfasdsas";

        @Override
        public void a() {

        }

        private class T3 {

        }
    }
}