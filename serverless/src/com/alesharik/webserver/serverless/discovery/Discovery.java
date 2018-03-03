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

package com.alesharik.webserver.serverless.discovery;

import com.alesharik.webserver.serverless.exception.AgentException;

import javax.annotation.Nonnull;

/**
 * The discovery finds another agents in the network
 */
public interface Discovery {
    /**
     * Start discovery client(if needed)
     *
     * @throws AgentException if something went wrong
     */
    default void startDiscoveryClient() throws AgentException {
    }

    /**
     * Shutdown discovery client(if needed)
     *
     * @throws AgentException if something went wrong
     */
    default void shutdownDiscoveryClient() throws AgentException {
    }

    /**
     * Shutdown discovery client immediately(if needed)
     *
     * @throws AgentException if something went wrong
     */
    default void shutdownDiscoveryClientNow() throws AgentException {
        shutdownDiscoveryClient();
    }

    /**
     * Start main discovery(if needed)
     *
     * @throws AgentException if something went wrong
     */
    default void startDiscovery() throws AgentException {
    }

    /**
     * Try to discover another agents
     *
     * @param mediator the discovery mediator
     * @throws AgentException if something went wrong
     */
    void discover(@Nonnull DiscoveryMediator mediator) throws AgentException;

    /**
     * Shutdown main discovery(if needed)
     *
     * @throws AgentException if something went wrong
     */
    default void shutdownDiscovery() throws AgentException {
    }

    /**
     * Shutdown main discovery immediately(if needed)
     *
     * @throws AgentException if something went wrong
     */
    default void shutdownDiscoveryNow() throws AgentException {
        shutdownDiscovery();
    }
}
