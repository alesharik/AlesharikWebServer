package com.alesharik.webserver.api.control.messaging;

import java.io.Serializable;

/**
 * This is base of ControlSocket messages. If you want to create new message for ControlSocket, you need to implement it
 * in message class
 */
public interface ControlSocketMessage extends Serializable {
}
