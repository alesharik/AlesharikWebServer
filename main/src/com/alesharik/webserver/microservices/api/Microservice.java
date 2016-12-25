package com.alesharik.webserver.microservices.api;

public abstract class Microservice {
    public abstract void setup();

    public abstract void handleEventAsync(MicroserviceEvent message);
}
