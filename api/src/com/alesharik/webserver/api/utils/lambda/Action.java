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

import javax.annotation.Nullable;

/**
 * Action represents event, which can be listened by {@link When}
 *
 * @param <C> argument type
 * @param <R> {@link When} return type
 * @see LambdaUtils
 */
@NotThreadSafe
public interface Action<C, R> {
    /**
     * Call this action. This is equivalent to {@link LambdaUtils#fire(Action, Object)} method
     *
     * @param argument action argument
     */
    void call(@Nullable C argument);
}
