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

package com.alesharik.webserver.api.serial;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SerialTest {

    public static final double DOUBLE_STEP = 1.79769313486232 * Math.pow(10, 304);
    public static final double FLOAT_STEP = 429496.7296;
    public static final int INT_STEP = 500_000;
    public static final double LONG_STEP = 2L * Math.pow(10, 15);

    @Test
    public void serializeDefaultArray() {
        T t = new T(new byte[] {1, 2, 3, 4, 5}, 12, new A(2, 345L));
        Serializer serializer = Serial.getSerializer(T.class);
        byte[] serialize = serializer.serializeDefault(t);
        assertNotNull(serialize);
        T deser = (T) serializer.deserializeDefault(serialize);
        assertEquals(t, deser);
    }

    @Test
    public void serializeDefaultArrayNull() {
        T t = new T(null, 12, new A(2, 345L));
        Serializer serializer = Serial.getSerializer(T.class);
        byte[] serialize = serializer.serializeDefault(t);
        assertNotNull(serialize);
        T deser = (T) serializer.deserializeDefault(serialize);
        assertEquals(t, deser);
    }

    @Test
    public void serializeDefaultArrayWithNull() {
        Q q = new Q(new C[]{null, new C(), null}, 12);
        Serializer serializer = Serial.getSerializer(Q.class);
        byte[] serialize = serializer.serializeDefault(q);
        assertNotNull(serialize);
        Q deser = (Q) serializer.deserializeDefault(serialize);
        assertEquals(q, deser);
    }

    @Test
    public void serializeDefaultNested() {
        Serializer serializer = Serial.getSerializer(G.class);
        G o = new G(new A(2, 245L));
        byte[] serialize = serializer.serializeDefault(o);
        assertNotNull(serialize);
        G deser = (G) serializer.deserializeDefault(serialize);
        assertEquals(o, deser);
    }

    @Test
    public void serializeDefaultNestedNull() {
        Serializer serializer = Serial.getSerializer(G.class);
        G o = new G(null);
        byte[] serialize = serializer.serializeDefault(o);
        assertNotNull(serialize);
        G deser = (G) serializer.deserializeDefault(serialize);
        assertEquals(o, deser);
    }

    @Test
    public void serializeDefault() {
        Serializer serializer = Serial.getSerializer(A.class);
        A o = new A(2, 345L);
        byte[] serialize = serializer.serializeDefault(o);
        assertNotNull(serialize);
        A a = (A) serializer.deserializeDefault(serialize);
        assertEquals(o, a);
    }

    @Test
    public void serializeDefaultEmpty() {
        Serializer serializer = Serial.getSerializer(C.class);
        C o = new C();
        byte[] serialize = serializer.serializeDefault(o);
        assertNotNull(serialize);
        C c = (C) serializer.deserializeDefault(serialize);
        assertEquals(o, c);
    }

    @Test
    public void serializeInterface() {
        IH o = new IH();
        byte[] serialize = Serial.serialize(o);
        IH deserialize = Serial.deserialize(serialize);
        assertEquals(o, deserialize);
    }

    @Test
    public void serializeList() {
        ListTest test = new ListTest();
        byte[] serialize = Serial.serialize(test);
        ListTest deserialize = Serial.deserialize(serialize);
        assertEquals(test, deserialize);
    }

    @Test
    public void serializePrimitiveBoolean() {
        byte[] serialize = Serial.serialize(true);
        assertEquals(8 + 8 + 1, serialize.length);
        assertTrue(Serial.<Boolean>deserialize(serialize));
        byte[] serialize1 = Serial.serialize(false);
        assertEquals(8 + 8 + 1, serialize1.length);
        assertFalse(Serial.<Boolean>deserialize(serialize1));
    }

    @Test
    public void serializePrimitiveChar() {
        for(int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++) {
            byte[] serialize = Serial.serialize((char) i);
            assertEquals(8 + 8 + 2, serialize.length);
            assertEquals(i, Serial.<Character>deserialize(serialize).charValue());
        }
    }

    @Test
    public void serializePrimitiveShort() {
        for(int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
            byte[] serialize = Serial.serialize((short) i);
            assertEquals(8 + 8 + 2, serialize.length);
            assertEquals(i, Serial.<Short>deserialize(serialize).shortValue());
        }
    }

    @Test
    public void serializePrimitiveInt() {
        for(int i = Integer.MIN_VALUE; i <= Integer.MAX_VALUE - INT_STEP; i += INT_STEP) {
            byte[] serialize = Serial.serialize(i);
            assertEquals(8 + 8 + 4, serialize.length);
            assertEquals(i, Serial.<Integer>deserialize(serialize).intValue());
        }
    }

    @Test
    public void serializePrimitiveFloat() {
        for(float i = Float.MIN_VALUE; i <= Short.MAX_VALUE - FLOAT_STEP; i += FLOAT_STEP) {
            byte[] serialize = Serial.serialize(i);
            assertEquals(8 + 8 + 4, serialize.length);
            assertEquals(i, Serial.<Float>deserialize(serialize), 0.00001);
        }
    }

    @Test
    public void serializePrimitiveLong() {
        for(long i = Long.MIN_VALUE; i <= Long.MAX_VALUE - LONG_STEP; i += LONG_STEP) {
            byte[] serialize = Serial.serialize(i);
            assertEquals(8 + 8 + 8, serialize.length);
            assertEquals(i, Serial.<Long>deserialize(serialize).longValue());
        }
    }

    @Test
    public void serializePrimitiveDouble() {
        for(double i = Double.MIN_VALUE; i <= Double.MAX_VALUE - DOUBLE_STEP; i += DOUBLE_STEP) {
            byte[] serialize = Serial.serialize(i);
            assertEquals(8 + 8 + 8, serialize.length);
            assertEquals(i, Serial.<Double>deserialize(serialize), 0.0001);
        }
    }

    @Test
    public void serializeString() {
        byte[] tests = Serial.serialize("test");
        assertEquals("test", Serial.deserialize(tests));
    }

    @Test
    public void serializeClass() {
        byte[] serialize = Serial.serialize(TypeNotPresentException.class);
        assertEquals(TypeNotPresentException.class, Serial.deserialize(serialize));
    }

    @Getter
    @EqualsAndHashCode
    @ToString
    public static class ListTest implements Serializable {
        public final List<String> list = new ArrayList<>();

        public ListTest() {
            list.add(RandomStringUtils.random(12));
        }
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class IH implements Serializable {
        private final I i;

        public IH() {
            this.i = new I1("wat");
        }
    }

    public interface I extends Serializable {

    }

    @EqualsAndHashCode
    @ToString
    @AllArgsConstructor
    public static class I1 implements I {
        private final String test;

        public I1() {
            test = "test";
        }
    }

    public static class C implements Serializable {
        @Override
        public int hashCode() {
            return 31;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof C;
        }
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class G implements Serializable {
        private final A a;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class T implements Serializable {
        private final byte[] a;
        private final int b;
        private final A c;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class Q implements Serializable {
        public final C[] a;
        public final int b;

        public Q() {
            a = new C[0];
            b = 0;
        }
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class A implements Serializable {
        private final int a;
        private final long b;
    }
}