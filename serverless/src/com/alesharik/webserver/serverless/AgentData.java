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

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

/**
 * Contains agent shared data
 */
public interface AgentData {
    /**
     * Return agent ID
     *
     * @return agent ID
     */
    @Nonnull
    UUID getId();

    /**
     * Return the agent name
     *
     * @return the agent name
     */
    @Nonnull
    String getName();

    /**
     * Set agent name
     *
     * @param name new agent name
     */
    void setName(@Nonnull String name);

    /**
     * Return custom data storage
     *
     * @return the custom data storage
     */
    Map<String, String> getCustomData();
}
