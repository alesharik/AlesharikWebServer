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

package com.alesharik.webserver.module.http.addon.websocket.processor;

import com.alesharik.webserver.module.http.addon.MessageProcessorContext;
import com.alesharik.webserver.module.http.http.Request;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
@ToString
public class WebSocketMessageProcessorContext implements MessageProcessorContext {
    private static final AtomicLong counter = new AtomicLong();
    @Getter
    protected final Request handshakeRequest;
    @Getter
    protected final long id = counter.getAndIncrement();
    protected final Map<String, Object> map = new ConcurrentHashMap<>();

    @Override
    public void setParameter(String name, Object value) {
        if(value == null)
            map.remove(name);
        else
            map.put(name, value);
    }

    @Override
    public <T> T getParameter(String name) {
        //noinspection unchecked
        return (T) map.get(name);
    }
}
