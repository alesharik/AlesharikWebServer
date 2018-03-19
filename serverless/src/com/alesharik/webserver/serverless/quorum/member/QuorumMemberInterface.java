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

package com.alesharik.webserver.serverless.quorum.member;

import com.alesharik.webserver.serverless.RemoteAgent;
import com.alesharik.webserver.serverless.message.MessageManager;
import com.alesharik.webserver.serverless.quorum.QuorumDecision;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This interface is accessible to all quorum members. All data transfer between quorum members is secured by quorum MASTER KEYPAIR
 */
public interface QuorumMemberInterface {
    /**
     * Add decision maker to agent
     *
     * @param type          message type
     * @param decisionMaker the decision maker
     */
    void addDecisionMaker(@Nonnull String type, @Nonnull DecisionMaker decisionMaker);

    /**
     * Remove decision maker from agent
     *
     * @param type          message type
     * @param decisionMaker the decision maker
     */
    void removeDeicisonMaker(@Nonnull String type, @Nonnull DecisionMaker decisionMaker);

    /**
     * Return quorum members data access point
     *
     * @return the data acess point
     */
    @Nonnull
    QuorumMemberData getData();

    /**
     * Return all quorum members
     *
     * @return the quorum members
     */
    @Nonnull
    List<RemoteAgent> getMembers();

    /**
     * Return {@link QuorumDecision} broadcaster
     *
     * @return the {@link QuorumDecision} broadcaster
     */
    @Nonnull
    DecisionSender getSender();

    /**
     * Return quorum message manager over secure sockets
     *
     * @return the secure {@link MessageManager} for quorum members only
     */
    @Nonnull
    MessageManager getSecureMessageManager();

    interface DecisionMaker {
        /**
         * Process decision
         *
         * @param decision             the decision
         * @param sender               the broadcaster
         * @param members              quorum members
         * @param secureMessageManager the secure message manager
         */
        void makeDecision(@Nonnull QuorumDecision decision, @Nonnull DecisionSender sender, @Nonnull List<RemoteAgent> members, @Nonnull MessageManager secureMessageManager);
    }

    interface DecisionSender {
        /**
         * Broadcaster decision to all agents
         *
         * @param decision the decision
         */
        void sendDecision(@Nonnull QuorumDecision decision);
    }
}
