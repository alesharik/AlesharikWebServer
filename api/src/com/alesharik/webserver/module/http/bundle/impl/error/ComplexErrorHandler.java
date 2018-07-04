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

import com.alesharik.webserver.module.http.bundle.ErrorHandler;
import com.alesharik.webserver.module.http.bundle.processor.Handler;
import com.alesharik.webserver.module.http.bundle.processor.HttpErrorHandler;
import com.alesharik.webserver.module.http.http.Request;
import com.alesharik.webserver.module.http.http.Response;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ComplexErrorHandler {
    private final List<ErrorPageProvider> providers = new ArrayList<>();
    private final List<ErrorVisitor> visitors = new ArrayList<>();
    @Getter
    private final Handler errorWrapper = new HandlerImpl();
    @Getter
    private final ErrorHandler errorHandler = new ErrorHandlerImpl();
    @Getter
    private final HttpErrorHandler httpErrorHandler = new HttpErrorHandlerImpl();

    public void addProvider(@Nonnull ErrorPageProvider provider) {
        providers.add(provider);
        providers.sort(Comparator.comparingInt(ErrorPageProvider::getPriority));
    }

    public void addVisitor(ErrorVisitor visitor) {
        visitors.add(visitor);
    }

    private final class HandlerImpl implements Handler {

        @Override
        public void handle(@Nonnull Request request, @Nonnull Response response) throws Exception {
            if(response.getResponseCode() >= 400) {
                for(ErrorVisitor visitor : visitors)
                    visitor.visitError(request, response);
                for(ErrorPageProvider provider : providers) {
                    if(provider.isApplicable(request, response)) {
                        provider.sendErrorPage(request, response);
                        return;
                    }
                }
            }
        }
    }

    private final class ErrorHandlerImpl implements ErrorHandler {

        @Override
        public void handleException(Exception e, Request request, Response response, Pool pool) {
            for(ErrorVisitor visitor : visitors)
                visitor.visitException(request, response, e);
            for(ErrorPageProvider provider : providers) {
                if(provider.canSendExceptionErrorPages()) {
                    provider.sendExceptionErrorPage(request, response, e);
                    return;
                }
            }
        }
    }

    private final class HttpErrorHandlerImpl implements HttpErrorHandler {

        @Override
        public void handleException(@Nonnull Exception e, @Nonnull Request request, @Nonnull Response response) {
            for(ErrorVisitor visitor : visitors)
                visitor.visitException(request, response, e);
            for(ErrorPageProvider provider : providers) {
                if(provider.canSendExceptionErrorPages()) {
                    provider.sendExceptionErrorPage(request, response, e);
                    return;
                }
            }
        }
    }
}
