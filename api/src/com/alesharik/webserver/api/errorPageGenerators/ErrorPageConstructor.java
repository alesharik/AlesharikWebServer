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

package com.alesharik.webserver.api.errorPageGenerators;


import org.glassfish.grizzly.http.server.Request;

import javax.annotation.concurrent.ThreadSafe;

/**
 * This interface used for generate error pages for one status
 */
@ThreadSafe
public interface ErrorPageConstructor {
    /**
     * Generate error page with description and without exception
     *
     * @param request      the request
     * @param status       status code
     * @param reasonPhrase reason phrase
     * @param description  description
     * @param throwable    throwable
     * @return HTML code of error page
     */
    String generate(Request request, int status, String reasonPhrase, String description, Throwable throwable);

    /**
     * Return true if support this code
     */
    boolean support(int status);

    /**
     * Return name of constructor
     */
    String getName();
}
