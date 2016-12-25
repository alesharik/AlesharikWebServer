package com.alesharik.webserver.api.functions;

import java.util.Objects;

/**
 * This is three-argument specialization of {@link java.util.function.Consumer}
 *
 * @param <A> first argument
 * @param <B> second argument
 * @param <C> third argument
 * @see java.util.function.Consumer
 */
@FunctionalInterface
public interface TripleConsumer<A, B, C> {
    void accept(A a, B b, C c);

    default TripleConsumer<A, B, C> andThen(TripleConsumer<? super A, ? super B, ? super C> consumer) {
        Objects.requireNonNull(consumer);

        return (a, b, c) -> {
            accept(a, b, c);
            consumer.accept(a, b, c);
        };
    }
}
