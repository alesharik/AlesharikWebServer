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

package com.alesharik.webserver.serverless.message;

import com.alesharik.webserver.serverless.RemoteAgent;
import com.alesharik.webserver.serverless.exception.AgentException;

import javax.annotation.Nonnull;

/**
 * Manages global message stuff and message parsers registration
 */
public interface MessageManager {
    /**
     * Register new message factory to parse messages
     *
     * @param factory the factory
     */
    void registerFactory(@Nonnull MessageFactory<?> factory);

    /**
     * Unregister the message factory
     *
     * @param factory the factory
     */
    void unregisterFactory(@Nonnull MessageFactory<?> factory);

    /**
     * Open new {@link MessageManager} to {@link RemoteAgent}
     *
     * @param agent the agent
     * @return opened channel
     */
    @Nonnull
    MessageChannel openChannel(@Nonnull RemoteAgent agent) throws AgentException;
}
