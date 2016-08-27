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
