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

package com.alesharik.webserver.serverless;

import com.alesharik.webserver.serverless.exception.AgentException;
import com.alesharik.webserver.serverless.rating.RemoteRatingService;

import javax.annotation.Nonnull;
import java.net.InetAddress;
import java.util.UUID;

/**
 * Remote agent contains information about actual remote agent
 */
public interface RemoteAgent {
    /**
     * Forget the agent
     */
    void forget();

    /**
     * Set agent alive status
     *
     * @param alive alive status
     */
    void setAlive(boolean alive);

    /**
     * Return <code>true</code> if agent is alive
     *
     * @return <code>true</code> if agent is alive
     */
    boolean getAlive();

    /**
     * Return agent ID
     *
     * @return agent ID
     */
    @Nonnull
    UUID getId();

    /**
     * Return agent data
     *
     * @param forceFetch <code>true</code> - always fetch, false - use cached version
     * @return the agent data
     */
    @Nonnull
    AgentData getAgentData(boolean forceFetch) throws AgentException;

    /**
     * Return agent address
     *
     * @return the agent address
     */
    @Nonnull
    InetAddress getAddress();

    @Nonnull
    RemoteRatingService getRatingService();
}
