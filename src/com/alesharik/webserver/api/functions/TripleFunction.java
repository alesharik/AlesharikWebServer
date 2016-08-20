package com.alesharik.webserver.api.functions;

import java.util.Objects;
import java.util.function.Function;

/**
 * This is a three-argument specialization of {@link Function}
 *
 * @param <A> first argument
 * @param <B> second argument
 * @param <C> third argument
 * @param <R> result
 * @see Function
 */
@FunctionalInterface
public interface TripleFunction<A, B, C, R> {
    R apply(A a, B b, C c);

    default <V> TripleFunction<A, B, C, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);

        return (A a, B b, C c) -> after.apply(apply(a, b, c));
    }
}
