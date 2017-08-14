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
