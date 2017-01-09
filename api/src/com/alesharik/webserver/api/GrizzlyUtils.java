package com.alesharik.webserver.api;

import org.glassfish.grizzly.http.Cookie;

public class GrizzlyUtils {
    /**
     * Find cookie for name in cookies list
     *
     * @return {@link Cookie} if it find needed cookie, overwise <code>null</code>
     */
    public static Cookie getCookieForName(String name, Cookie[] cookies) {
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(name)) {
                return cookie;
            }
        }
        return null;
    }
}
