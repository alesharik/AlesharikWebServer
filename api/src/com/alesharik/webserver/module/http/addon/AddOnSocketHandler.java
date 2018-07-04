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

package com.alesharik.webserver.module.http.addon;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

/**
 * This handler overrides default HTTP handler and handles socket instead
 */
public interface AddOnSocketHandler {
    /**
     * Prepare socket handler
     *
     * @param context the context
     */
    void init(@Nonnull AddOnSocketContext context);

    /**
     * Handle socket message
     *
     * @param byteBuffer the message
     * @param context    the context
     */
    void handle(@Nonnull ByteBuffer byteBuffer, @Nonnull AddOnSocketContext context);

    /**
     * Close handler
     * @param context the context
     */
    void close(@Nonnull AddOnSocketContext context);
}
