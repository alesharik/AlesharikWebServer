package com.alesharik.webserver.api.misc;

/**
 * This class is store for three values<br>
 * This class designed for implementation
 */
public class Triple<A, B, C> {
    protected A a;
    protected B b;
    protected C c;

    protected Triple() {
    }

    protected Triple(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public static <A, B, C> Triple immutable(A a, B b, C c) {
        return new Triple<>(a, b, c);
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public C getC() {
        return c;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;

        if(a != null ? !a.equals(triple.a) : triple.a != null) return false;
        if(b != null ? !b.equals(triple.b) : triple.b != null) return false;
        return c != null ? c.equals(triple.c) : triple.c == null;

    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        result = 31 * result + (c != null ? c.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Triple{" +
                "a=" + a +
                ", b=" + b +
                ", c=" + c +
                '}';
    }
}
