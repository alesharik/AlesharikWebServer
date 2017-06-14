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

package com.alesharik.webserver.configuration.message;

import javax.annotation.Nonnull;

/**
 * {@link MessageStreamPair} create new {@link MessageStream} pair and connect it.
 *
 * @param <S> {@link MessageStream} class
 * @param <M> {@link Message} class
 */
public interface MessageStreamPair<M extends Message, S extends MessageStream> {
    /**
     * Return first stream
     */
    @Nonnull
    S first();

    /**
     * Return second stream
     */
    @Nonnull
    S second();
}
