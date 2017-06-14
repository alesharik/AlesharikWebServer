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
