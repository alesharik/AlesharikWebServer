package com.alesharik.webserver.control.socket;

public abstract class AbstractControlSocketHandler {
    protected final ControlSocketSender controlSocketSender;

    public AbstractControlSocketHandler(ControlSocketSender controlSocketSender) {
        this.controlSocketSender = controlSocketSender;
    }

    public abstract void onMessage(String message);

    public abstract void onMessage(Object message);

    public abstract void onOpen();

    public abstract void onClose();
}
