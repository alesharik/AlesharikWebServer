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

package com.alesharik.webserver.api.qt;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Class, which implements this interface, can be serialized in QDataStream. If class has empty constructor, when it will
 * be called for creating object
 */
public interface QSerializable extends Serializable {
    /**
     * Write values to {@link QDataStream}
     *
     * @param stream the stream
     */
    void write(@Nonnull QDataStream stream);

    /**
     * Read values from {@link QDataStream}. All local variables will be uninitialized if class doesn't have empty constructor
     *
     * @param stream the stream
     */
    void read(@Nonnull QDataStream stream);
}
