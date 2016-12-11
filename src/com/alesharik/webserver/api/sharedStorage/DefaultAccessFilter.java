package com.alesharik.webserver.api.sharedStorage;

/**
 * Disable external set only
 */
public class DefaultAccessFilter implements AccessFilter {
    @Override
    public boolean canAccess(Class<?> clazz, Type type, String filedName) {
        return type != Type.SET_EXTERNAL;
    }
}
