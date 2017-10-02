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

package com.alesharik.webserver.api.utils.ping;

import com.alesharik.webserver.api.utils.lambda.Action;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * Manages group of ping/pongs
 *
 * @see PingPongManager
 */
public interface PingPong {
    /**
     * Create new ping request
     *
     * @return ping request unique id
     */
    long ping();

    /**
     * Respond ping with specified id
     *
     * @param id ping request unique id
     */
    void pong(long id);

    /**
     * Called when ping is expired(has waiting time > timeout)
     */
    @Nonnull
    Action<Long, PingPong> pingTimeout();

    /**
     * Set PingPong group timeout
     *
     * @param timeout timeout in milliseconds
     */
    void setTimeout(long timeout);

    /**
     * Set PingPong group timeout
     *
     * @param timeout  timeout
     * @param timeUnit timeout time unit(>= {@link TimeUnit#MILLISECONDS})
     */
    default void setTimeout(long timeout, @Nonnull TimeUnit timeUnit) {
        setTimeout(timeUnit.toMillis(timeout));
    }
}
