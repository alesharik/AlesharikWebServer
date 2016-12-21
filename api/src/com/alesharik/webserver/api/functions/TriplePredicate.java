package com.alesharik.webserver.api.functions;

import java.util.Objects;

/**
 * This is a triple-argument specialization of {@link java.util.function.Predicate}
 *
 * @param <A>
 * @param <B>
 * @param <C>
 */
@FunctionalInterface
public interface TriplePredicate<A, B, C> {
    boolean test(A a, B b, C c);

    default TriplePredicate<A, B, C> and(TriplePredicate<? super A, ? super B, ? super C> predicate) {
        Objects.requireNonNull(predicate);

        return (a, b, c) -> test(a, b, c) && predicate.test(a, b, c);
    }

    default TriplePredicate<A, B, C> negate() {
        return (a, b, c) -> !test(a, b, c);
    }

    default TriplePredicate<A, B, C> or(TriplePredicate<? super A, ? super B, ? super C> predicate) {
        Objects.requireNonNull(predicate);
        return (a, b, c) -> test(a, b, c) || predicate.test(a, b, c);
    }
}
