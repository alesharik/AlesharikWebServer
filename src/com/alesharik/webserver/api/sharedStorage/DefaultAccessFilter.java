package com.alesharik.webserver.api.sharedStorage;

import com.sun.istack.internal.Nullable;

/**
 * Disable external set only
 */
public class DefaultAccessFilter implements AccessFilter {
    @Override
    public boolean canAccess(Class<?> clazz, Type type, @Nullable String filedName) {
        return type != Type.SET_EXTERNAL;
    }
}
