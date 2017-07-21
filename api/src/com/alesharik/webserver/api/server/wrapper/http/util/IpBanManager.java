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

package com.alesharik.webserver.api.server.wrapper.http.util;

import com.alesharik.webserver.api.collections.ConcurrentLiveArrayList;
import com.alesharik.webserver.api.server.wrapper.bundle.Filter;
import com.alesharik.webserver.api.server.wrapper.http.HttpStatus;
import com.alesharik.webserver.api.server.wrapper.http.Request;
import com.alesharik.webserver.api.server.wrapper.http.Response;

import javax.annotation.Nonnull;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * This filter type allows you to ban IP addresses
 */
public class IpBanManager implements Filter {
    private static final IpBanManager GLOBAL = new IpBanManager();

    /**
     * Return global IP ban manager for this application
     */
    public static IpBanManager global() {
        return GLOBAL;
    }

    protected final ConcurrentLiveArrayList<InetAddress> banned;

    /**
     * Create ban manager with 1 hour update time
     */
    public IpBanManager() {
        this(1, TimeUnit.HOURS);
    }

    /**
     * Create ban manager
     *
     * @param time     period time to update
     * @param timeUnit time unit. Minimum is {@link TimeUnit#MILLISECONDS}
     * @throws IllegalArgumentException if timeUnit < {@link TimeUnit#MILLISECONDS}
     */
    public IpBanManager(long time, @Nonnull TimeUnit timeUnit) {
        if(timeUnit == TimeUnit.MICROSECONDS || timeUnit == TimeUnit.NANOSECONDS)
            throw new IllegalArgumentException();

        this.banned = new ConcurrentLiveArrayList<>(timeUnit.toMillis(time), 16);
    }

    @Override
    public boolean accept(Request request, Response response) {
        boolean contains = banned.contains(request.getRemote().getAddress());
        if(contains) {
            response.respond(HttpStatus.TOO_MANY_REQUESTS_429);
            return false;
        }
        return true;
    }

    /**
     * Ban IP address for a day
     *
     * @param address the address to ban
     */
    public void ban(@Nonnull InetAddress address) {
        ban(address, 1, TimeUnit.DAYS);
    }

    /**
     * Ban IP address for an specified period
     *
     * @param address  the address to ban
     * @param time     ban period
     * @param timeUnit period time unit. Minimum is {@link TimeUnit#MILLISECONDS}
     * @throws IllegalArgumentException if timeUnit < {@link TimeUnit#MILLISECONDS}
     */
    public void ban(@Nonnull InetAddress address, long time, @Nonnull TimeUnit timeUnit) {
        if(timeUnit == TimeUnit.MICROSECONDS || timeUnit == TimeUnit.NANOSECONDS)
            throw new IllegalArgumentException();

        banned.add(address, timeUnit.toMillis(time));
    }
}
