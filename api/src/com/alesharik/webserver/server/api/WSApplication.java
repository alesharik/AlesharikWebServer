/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.server.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Register new WebSocketApplication. Your class must extend {@link org.glassfish.grizzly.websockets.WebSocketApplication}.
 * WSApplication registered by default
 *
 * @see org.glassfish.grizzly.websockets.WebSocketEngine#register(String, String, org.glassfish.grizzly.websockets.WebSocketApplication)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WSApplication {
    String contextPath() default "";

    /**
     * Url pattern
     */
    String value();

    /**
     * {@link WSChecker} class
     */
    Class<?> checker() default WSChecker.Enabled.class;
}
