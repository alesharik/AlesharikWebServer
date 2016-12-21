package com.alesharik.webserver.api.messages;

/**
 * This is a main class in Messages system. Extend this class to create specific messages.<br>
 * Your class need to be cloneable!
 */
public class Message implements Cloneable {
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
