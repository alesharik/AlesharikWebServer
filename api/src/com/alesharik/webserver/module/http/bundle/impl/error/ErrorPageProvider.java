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

package com.alesharik.webserver.module.http.bundle.impl.error;

import com.alesharik.webserver.module.http.http.Request;
import com.alesharik.webserver.module.http.http.Response;

public interface ErrorPageProvider {
    /**
     * More is better
     *
     * @return error page provider priority
     */
    default int getPriority() {
        return 1;
    }

    boolean isApplicable(Request request, Response response);

    void sendErrorPage(Request request, Response response) throws Exception;

    default boolean canSendExceptionErrorPages() {
        return false;
    }

    default void sendExceptionErrorPage(Request request, Response response, Exception e) {

    }
}
