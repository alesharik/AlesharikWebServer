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

import com.alesharik.webserver.serverless.discovery.Discovery;
import com.alesharik.webserver.serverless.exception.AgentException;
import com.alesharik.webserver.serverless.heartbeat.HeartbeatService;
import com.alesharik.webserver.serverless.message.MessageManager;
import com.alesharik.webserver.serverless.notify.NotifyManager;
import com.alesharik.webserver.serverless.quorum.Quorum;
import com.alesharik.webserver.serverless.quorum.member.QuorumMemberInterface;
import com.alesharik.webserver.serverless.rating.RatingService;
import com.alesharik.webserver.serverless.transport.Transport;
import com.alesharik.webserver.serverless.utils.Utility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This class represents actual serverless agent
 */
public interface ServerlessAgent {
    /**
     * Start the agent
     *
     * @throws AgentException if something went wrong
     */
    void start() throws AgentException;

    /**
     * Shutdown the agent
     *
     * @throws AgentException if something went wrong
     */
    void shutdown() throws AgentException;

    /**
     * Shutdown the agent immediately
     *
     * @throws AgentException if something went wrong
     */
    void shutdownNow() throws AgentException;

    /**
     * Return agent's notify manager
     *
     * @return the notify manager
     */
    @Nonnull
    NotifyManager getNotifyManager();

    /**
     * Return agent's discovery
     *
     * @return the discovery
     */
    @Nonnull
    Discovery getDiscovery();

    /**
     * Return agent's transport
     *
     * @return the transport
     */
    @Nonnull
    Transport getTransport();

    /**
     * Return agent data
     *
     * @return the agent data
     */
    @Nonnull
    AgentData getData();

    /**
     * Return agent's heartbeat service
     *
     * @return the heartbeat service
     */
    @Nonnull
    HeartbeatService getHeartbeatService();

    /**
     * Return agent's message manager
     *
     * @return the message manager
     */
    @Nonnull
    MessageManager getMessageManager();

    /**
     * Return known agents
     *
     * @return known agents
     */
    @Nonnull
    List<RemoteAgent> getAgents();

    /**
     * Return agent's utility if it exists
     *
     * @param utilityClass the utility class
     * @param <U>          utility type
     * @return the utility or <code>null</code> if it isn't registered
     */
    @Nullable
    <U extends Utility> U getUtility(@Nonnull Class<?> utilityClass);

    /**
     * Return the error handler
     *
     * @return the error handler
     */
    @Nonnull
    ExceptionHandler getExceptionHandler();

    @Nonnull
    RatingService getRatingService();

    @Nullable
    Quorum getQuorum();

    boolean isQuorumMember();

    @Nullable
    QuorumMemberInterface getQuorumMemberInterface();
}
