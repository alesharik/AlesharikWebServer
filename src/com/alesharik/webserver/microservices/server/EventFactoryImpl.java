package com.alesharik.webserver.microservices.server;

import com.lmax.disruptor.EventFactory;

class EventFactoryImpl implements EventFactory<Event> {
    @Override
    public Event newInstance() {
        return new Event();
    }
}
