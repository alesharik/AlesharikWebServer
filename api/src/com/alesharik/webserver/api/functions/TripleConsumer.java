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
