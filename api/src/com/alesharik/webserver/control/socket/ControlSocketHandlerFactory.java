package com.alesharik.webserver.control.socket;

/**
 * Class, implementing this interface, used for create new specific {@link AbstractControlSocketHandler}
 */
public interface ControlSocketHandlerFactory {
    /**
     * Create new instance of your class extending {@link AbstractControlSocketHandler}
     *
     * @param socketSender the sender
     * @param info         the socket information
     */
    AbstractControlSocketHandler newInstance(ControlSocketSender socketSender, ControlSocketInfo info);
}
