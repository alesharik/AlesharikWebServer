package com.alesharik.webserver.microservices.api;

public class OneArgMicorserviceEvent<A> extends MicroserviceEvent {
    private A arg0;

    public OneArgMicorserviceEvent(A arg0) {
        this.arg0 = arg0;
    }

    public A getArg0() {
        return arg0;
    }

    public void setArg0(A arg0) {
        this.arg0 = arg0;
    }
}
