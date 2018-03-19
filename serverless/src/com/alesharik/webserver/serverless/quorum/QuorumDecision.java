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

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.UUID;

/**
 * This class warps quorum decision object to send between quorum/agents
 */
public interface QuorumDecision extends Serializable {
    /**
     * Return decision classification. It is used for separate decisions for quorum
     *
     * @return decision classification
     */
    @Nonnull
    String getType();

    /**
     * Return decision unique ID
     *
     * @return unique ID
     */
    @Nonnull
    UUID getId();

    /**
     * Return who the decision refers to
     *
     * @return the referer
     */
    @Nonnull
    Referer getReferer();

    /**
     * Return message priority
     *
     * @return the message priority
     */
    @Nonnull
    Priority getPriority();

    enum Referer {
        /**
         * Decision refers to quorum. Use it to "ask" something from quorum
         */
        QUORUM,
        /**
         * Decision refers to agents. Use it as notification or "question" reponse
         */
        AGENTS
    }

    enum Priority {
        /**
         * Low message priority
         */
        LOW,
        /**
         * Medium message priority
         */
        MEDIUM,
        /**
         * High message priority
         */
        HIGH,
        /**
         * Critical message priority. Quorum can ignore it, agents must process it ASAP
         */
        CRITICAL
    }
}
