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

package com.alesharik.webserver.serverless.heartbeat;

import com.alesharik.webserver.serverless.RemoteAgent;
import com.alesharik.webserver.serverless.exception.AgentException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Heartbeat service checks if agent is alive
 */
public interface HeartbeatService {
    /**
     * Check the agent
     *
     * @param agent       the agent
     * @param elapsedTime elapsed time from last heartbeat
     * @throws AgentException if something went wrong
     */
    void heartbeat(@Nonnull RemoteAgent agent, @Nonnegative long elapsedTime) throws AgentException;
}
