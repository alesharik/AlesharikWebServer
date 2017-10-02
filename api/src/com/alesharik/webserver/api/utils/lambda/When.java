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

package com.alesharik.webserver.api.utils.lambda;

import net.jcip.annotations.NotThreadSafe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * When instances listen {@link Action}s
 *
 * @param <C> action argument type
 * @param <R> return type
 * @see LambdaUtils
 */
@NotThreadSafe
public interface When<C, R> {
    /**
     * Passed consumer will be called in {@link Action} fire stage
     *
     * @param c argument consumer
     * @return return object
     * @throws IllegalStateException if this instance already has consumer/batch
     */
    @Nullable
    R then(@Nonnull Consumer<C> c);

    /**
     * Created batch will be called in {@link Action} fire stage
     *
     * @param batchConsumer consumer, which can fill new {@link Batch}
     * @return return object
     * @throws IllegalStateException if this instance already has consumer/batch
     */
    @Nullable
    R batch(@Nonnull Consumer<Batch<C>> batchConsumer);

    /**
     * Batch allows to ordered execute multiple consumers in one {@link When} object
     *
     * @param <C> action argument type
     */
    @NotThreadSafe
    interface Batch<C> {
        /**
         * Passed consumer will be called in {@link Action} fire stage
         *
         * @param c the consumer
         * @return this instance
         */
        @Nonnull
        Batch<C> then(@Nonnull Consumer<C> c);
    }
}
