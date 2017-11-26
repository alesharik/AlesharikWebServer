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

package com.alesharik.webserver.api.server.wrapper.util;

import com.alesharik.webserver.api.server.wrapper.bundle.HttpVisitor;
import com.alesharik.webserver.api.server.wrapper.http.Request;
import com.alesharik.webserver.api.server.wrapper.http.Response;
import com.alesharik.webserver.api.server.wrapper.http.data.Cookie;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * This visitor provides requests with session storage. This storage contains data for user session
 */
@ThreadSafe//TODO customized cleanup
public final class SessionStorage implements HttpVisitor {
    private final Storage.Factory factory;
    private final int storeTime;

    private final ConcurrentLiveHashMap<String, Storage> sessions = new ConcurrentLiveHashMap<>();

    /**
     * @param factory   storage instances supply
     * @param storeTime session expire time
     * @param timeUnit  time unit of storeTime. Minimum is seconds
     */
    public SessionStorage(@Nonnull Storage.Factory factory, int storeTime, @Nonnull TimeUnit timeUnit) {
        this.factory = factory;
        this.storeTime = (int) timeUnit.toSeconds(storeTime);
    }

    /**
     * Return storage for request
     *
     * @param request the request
     * @return session storage for request session
     * @throws IllegalStateException if storage not found
     */
    @Nonnull
    public static Storage forRequest(@Nonnull Request request) {
        Storage storage = (Storage) request.getData("session-storage");
        if(storage == null)
            throw new IllegalStateException("SessionStorage is not set up!");
        else
            return storage;
    }

    /**
     * Return storage for request
     *
     * @param request the request
     * @return session storage for request session or <code>null</code>
     */
    @Nullable
    public static Storage forUntrustedRequest(@Nonnull Request request) {
        return (Storage) request.getData("session-storage");
    }

    @Override
    public void visit(@Nonnull Request request, @Nonnull Response response) {
        for(Cookie cookie : request.getCookies()) {
            if(cookie.getName().equals("_sessionID")) {
                Storage storage = sessions.get(cookie.getValue());
                if(storage != null) {
                    request.setData("session-storage", storage);
                    sessions.resetTime(cookie.getValue());
                    return;
                }
            }
        }

        Storage storage = factory.newInstance(request);
        String id = UUID.randomUUID().toString();
        sessions.put(id, storage, storeTime * 1000);
        request.setData(id, storage);

        Cookie cookie = new Cookie("_sessionID", id);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(storeTime);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
