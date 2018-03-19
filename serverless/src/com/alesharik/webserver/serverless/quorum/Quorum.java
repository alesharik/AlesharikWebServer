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

package com.alesharik.webserver.serverless.quorum;

import com.alesharik.webserver.serverless.RemoteAgent;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This object represents all functional to access quorum as agent
 */
public interface Quorum {
    /**
     * Check if agent is quorum member
     *
     * @param agent the agent
     * @return <code>true</code> - agent is quorum member, overwise <code>false</code>
     */
    boolean isQuorumMember(@Nonnegative RemoteAgent agent);

    /**
     * Return quorum "public" shared data
     *
     * @return the quorum data object
     */
    @Nonnull
    QuorumData getData();

    /**
     * Ask decision from quorum
     *
     * @param decision the decision
     */
    void askDecision(@Nonnull QuorumDecision decision);

    /**
     * Add decision listener to agent
     *
     * @param listener the decision listener
     */
    void addDecisionListener(@Nonnull DecisionListener listener);

    /**
     * Remove decision listener from agent
     *
     * @param listener the decision listener
     */
    void removeDecisionListener(@Nonnull DecisionListener listener);

    /**
     * Send decision to quorum
     *
     * @param decision the decision
     */
    void sendDecision(@Nonnull QuorumDecision decision);

    interface DecisionListener {
        /**
         * Listen decision from quorum
         *
         * @param decision the decision
         */
        void listen(@Nonnull QuorumDecision decision);
    }
}
