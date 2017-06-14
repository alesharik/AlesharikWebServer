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

package com.alesharik.webserver.microservices.server;

import com.alesharik.webserver.microservices.api.Microservice;
import com.lmax.disruptor.EventHandler;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

class DisruptorEventHandler implements EventHandler<Event> {
    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("MicroserviceHandlers");
    private final ConcurrentHashMap<String, Microservice> microservices;

    public DisruptorEventHandler(ConcurrentHashMap<String, Microservice> microservices) {
        this.microservices = microservices;
    }

    @Override
    public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
        Enumeration<String> enumeration = microservices.keys();
        while(enumeration.hasMoreElements()) {
            String nextElement = enumeration.nextElement();
            if(nextElement.equals(event.getName())) {
                Microservice microservice = microservices.get(nextElement);
                new Thread(THREAD_GROUP, () -> microservice.handleEventAsync(event.getEvent())).start();
            }
        }
    }
}
