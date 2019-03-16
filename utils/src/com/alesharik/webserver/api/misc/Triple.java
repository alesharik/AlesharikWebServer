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

package com.alesharik.webserver.api.misc;

import com.alesharik.webserver.exception.error.BadImplementationError;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.Serializable;

/**
 * This class used for store three values
 */
public abstract class Triple<A, B, C> implements Cloneable {
    public Triple() {
    }

    /**
     * Create new immutable triple(you cannot change values)
     *
     * @param a   first value
     * @param b   second value
     * @param c   third value
     * @param <A> first value type
     * @param <B> second value type
     * @param <C> third value type
     * @return new triple
     */
    @Nonnull
    public static <A, B, C> Triple<A, B, C> immutable(@Nullable A a, @Nullable B b, @Nullable C c) {
        return new ImmutableTriple<>(a, b, c);
    }

    /**
     * Create new immutable serializable triple(you cannot change values, but you can serialize it)
     *
     * @param a   first value
     * @param b   second value
     * @param c   third value
     * @param <A> first value type
     * @param <B> second value type
     * @param <C> third value type
     * @return new triple
     */
    @Nonnull
    public static <A extends Object & Serializable, B extends Object & Serializable, C extends Object & Serializable> Triple<A, B, C> immutableSerializable(@Nullable A a, @Nullable B b, @Nullable C c) {
        return new ImmutableSerializableTriple<>(a, b, c);
    }

    /**
     * Create new mutable triple, where you can change values
     *
     * @param a   first value
     * @param b   second value
     * @param c   third value
     * @param <A> first value type
     * @param <B> second value type
     * @param <C> third value type
     * @return new triple
     */
    @Nonnull
    public static <A, B, C> MutableTriple<A, B, C> mutable(@Nullable A a, @Nullable B b, @Nullable C c) {
        return new MutableTriple<>(a, b, c);
    }


    /**
     * Create new mutable triple, where you can change values. You can serialize it
     *
     * @param a   first value
     * @param b   second value
     * @param c   third value
     * @param <A> first value type
     * @param <B> second value type
     * @param <C> third value type
     * @return new triple
     */
    @Nonnull
    public static <A extends Object & Serializable, B extends Object & Serializable, C extends Object & Serializable> MutableTriple<A, B, C> mutableSerializable(@Nullable A a, @Nullable B b, @Nullable C c) {
        return new MutableSerializableTriple<>(a, b, c);
    }

    public abstract A getA();

    public abstract B getB();

    public abstract C getC();

    @SuppressWarnings("unchecked")
    public Triple<A, B, C> clone() {
        try {
            return (Triple<A, B, C>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new BadImplementationError("Clone method must be implemented!");
        }
    }

    public boolean equals(Object o) {
        return o == this || o instanceof Triple && getA().equals(((Triple) o).getA()) && getB().equals(((Triple) o).getB()) && getC().equals(((Triple) o).getC());
    }

    @Override
    public int hashCode() {
        int result = getA() != null ? getA().hashCode() : 0;
        result = 31 * result + (getB() != null ? getB().hashCode() : 0);
        result = 31 * result + (getC() != null ? getC().hashCode() : 0);
        return result;
    }

    @AllArgsConstructor
    @ToString
    @Getter
    private static class ImmutableTriple<A, B, C> extends Triple<A, B, C> implements Cloneable {
        protected final A a;
        protected final B b;
        protected final C c;

        @SuppressWarnings("unused")
        protected ImmutableTriple() {
            a = null;
            b = null;
            c = null;
        }

        @Override
        public Triple<A, B, C> clone() {
            return new ImmutableTriple<>(a, b, c);
        }
    }

    @ToString
    @Getter
    private static final class ImmutableSerializableTriple<A extends Object & Serializable, B extends Object & Serializable, C extends Object & Serializable> extends ImmutableTriple<A, B, C> implements Serializable, Cloneable {
        private static final long serialVersionUID = -6012140996629786458L;

        public ImmutableSerializableTriple(A a, B b, C c) {
            super(a, b, c);
        }

        @Override
        public Triple<A, B, C> clone() {
            return new ImmutableSerializableTriple<>(a, b, c);
        }
    }

    @Getter
    @Setter
    @ToString
    @ThreadSafe
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class MutableTriple<A, B, C> extends Triple<A, B, C> implements Cloneable {
        protected A a;
        protected B b;
        protected C c;

        @SuppressWarnings("unused")
        protected MutableTriple() {
        }

        protected Object clone0() {
            return super.clone();
        }

        @SuppressWarnings("unchecked")
        @Override
        public MutableTriple<A, B, C> clone() {
            MutableTriple<A, B, C> clone = (MutableTriple<A, B, C>) clone0();
            clone.a = a;
            clone.b = b;
            clone.c = c;
            return clone;
        }
    }

    @ToString
    @ThreadSafe
    private static final class MutableSerializableTriple<A extends Object & Serializable, B extends Object & Serializable, C extends Object & Serializable> extends MutableTriple<A, B, C> implements Serializable, Cloneable {
        private static final long serialVersionUID = 2186491658888848308L;

        protected MutableSerializableTriple(A a, B b, C c) {
            super(a, b, c);
        }

        @SuppressWarnings("unchecked")
        @Override
        public MutableTriple<A, B, C> clone() {
            MutableSerializableTriple<A, B, C> clone = (MutableSerializableTriple<A, B, C>) clone0();
            clone.a = a;
            clone.b = b;
            clone.c = c;
            return clone;
        }
    }
}
