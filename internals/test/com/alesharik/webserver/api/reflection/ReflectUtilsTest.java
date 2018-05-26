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

import com.alesharik.webserver.test.TestUtils;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ReflectUtilsTest {
    @Test
    public void getMethods() throws Exception {
        List<Method> methods = ReflectUtils.getAllDeclaredMethods(Test5.class);
        assertContainsObjectSet(methods, new Method[]{
                C.class.getDeclaredMethod("a"),
                Test5.class.getDeclaredMethod("b"),
                Test4.class.getDeclaredMethod("c"),
                Test5.class.getDeclaredMethod("asd")
        });
    }

    @Test
    public void getFields() throws Exception {
        List<Field> fields = ReflectUtils.getAllDeclaredFields(Test5.class);
        assertContainsObjectSet(fields, new Field[]{
                C.class.getDeclaredField("TEST"),
                Test3.class.getDeclaredField("a1"),
                Test4.class.getDeclaredField("a1"),
                Test5.class.getDeclaredField("a1"),
                Test5.class.getDeclaredField("b1")
        });
    }

    @Test
    public void getInnerClasses() throws Exception {
        List<Class<?>> classes = ReflectUtils.getAllInnerClasses(Test5.class);
        assertContainsObjectSet(classes, new Class[]{
                C.I1.class,
                Test5.T2.class,
                Test4.T3.class,
                Test3.T3.class
        });
    }

    private <T> void assertContainsObjectSet(List<T> a, T[] b) {
        List<T> contains = new ArrayList<>();
        for(T t : a) {
            if(contains.contains(t))
                throw new AssertionError("Object contains more than 1 times!");
            for(T t1 : b) {
                if(t.equals(t1)) {
                    contains.add(t);
                    break;
                }
            }
        }
        assertEquals(contains.size(), b.length);
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