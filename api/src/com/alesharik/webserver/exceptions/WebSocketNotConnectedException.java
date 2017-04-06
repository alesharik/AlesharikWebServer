package com.alesharik.webserver.exceptions;

public class WebSocketNotConnectedException extends RuntimeException {
    public WebSocketNotConnectedException() {
    }

    public WebSocketNotConnectedException(String message) {
        super(message);
    }
}
