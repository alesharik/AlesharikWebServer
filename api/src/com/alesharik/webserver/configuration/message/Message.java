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

import javax.annotation.concurrent.Immutable;

/**
 * Message been used as container for hold data sending between two {@link MessageStream}s.
 * This class is immutable.
 * Use <code>newMessage</code> instead of constructor
 *
 * @param <T>
 */
@Immutable
public interface Message<T> {
    /**
     * Create new empty message. Used instead of constructor.
     */
    T newMessage();
}
