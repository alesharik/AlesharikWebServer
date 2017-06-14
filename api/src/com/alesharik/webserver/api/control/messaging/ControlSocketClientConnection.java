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

package com.alesharik.webserver.api.control.messaging;

import net.jcip.annotations.NotThreadSafe;

import javax.annotation.Nonnull;

/**
 * Client side connection
 */
@NotThreadSafe
public interface ControlSocketClientConnection extends ControlSocketConnection {

    /**
     * Add new listener
     *
     * @param listener the listener
     */
    void addListener(Listener listener);

    void removeListener(Listener listener);

    boolean containsListener(Listener listener);

    /**
     * Wait for socket connected and authenticated
     */
    void awaitConnection();

    /**
     * Listen received messages
     */
    interface Listener {
        /**
         * Can this listener listen message. <code>true</code> - message instance handled by {@link #listen(ControlSocketMessage)} method,
         * <code>false</code> - ignore listener
         * @param messageClazz message class
         */
        boolean canListen(Class<?> messageClazz);

        /**
         * Listen message class. It receive original server message and you can cast it to your type
         *
         * @param message the message
         */
        void listen(@Nonnull ControlSocketMessage message);
    }

    /**
     * Contains data for authentication
     */
    interface Authenticator {
        /**
         * Return server password
         */
        String getPassword();

        /**
         * Return server login
         */
        String getLogin();
    }
}
