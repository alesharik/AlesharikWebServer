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

package com.alesharik.webserver.module.http.bundle.processor;

import com.alesharik.webserver.module.http.bundle.processor.impl.ReThrowException;
import com.alesharik.webserver.module.http.http.Request;
import com.alesharik.webserver.module.http.http.Response;

import javax.annotation.Nonnull;

/**
 * Handlers usually are using to do primary request handling
 */
@FunctionalInterface
public interface Handler extends HttpProcessor {
    /**
     * Handle the request
     * @param request the request
     * @param response the bound response
     * @throws Exception may exception will be handled by {@link HttpErrorHandler} or {@link com.alesharik.webserver.module.http.bundle.ErrorHandler}
     */
    void handle(@Nonnull Request request, @Nonnull Response response) throws Exception;

    @Override
    default void process(@Nonnull Request request, @Nonnull Response response) {
        try {
            handle(request, response);
        } catch (Exception e) {
            throw new ReThrowException(e);
        }
    }
}
