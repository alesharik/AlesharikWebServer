package com.alesharik.webserver.microservices.server;

import com.alesharik.webserver.microservices.api.MicroserviceEvent;

class Event {
    private MicroserviceEvent event;
    private String name;

    Event(MicroserviceEvent event, String name) {
        this.event = event;
        this.name = name;
    }

    public Event() {
    }

    public MicroserviceEvent getEvent() {
        return event;
    }

    public String getName() {
        return name;
    }

    public void setEvent(MicroserviceEvent event) {
        this.event = event;
    }

    public void setName(String name) {
        this.name = name;
    }
}
