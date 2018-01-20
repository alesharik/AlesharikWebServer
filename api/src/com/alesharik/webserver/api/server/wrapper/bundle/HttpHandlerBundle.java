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

package com.alesharik.webserver.api.server.wrapper.bundle;

import com.alesharik.webserver.api.server.wrapper.addon.MessageProcessor;
import com.alesharik.webserver.api.server.wrapper.bundle.processor.HttpProcessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This must be annotated with {@link HttpBundle} annotation!
 */
public interface HttpHandlerBundle {
    Validator getValidator();

    @Deprecated
    default RequestRouter getRouter() {
        return null;
    }

    ;

    @Deprecated
    default FilterChain[] getFilterChains() {
        return new FilterChain[0];
    }

    ;

    @Deprecated
    default HttpHandler[] getHttpHandlers() {
        return new HttpHandler[0];
    }

    ;

    @Nonnull
    ErrorHandler getErrorHandler();

    @Deprecated
    default HttpHandlerResponseDecorator getReponseDecorator() {
        return HttpHandlerResponseDecorator.Ignore.INSTANCE;
    }

    @Nullable//temporary
    default HttpProcessor getProcessor() {
        return null;
    }

    @Nullable
    default MessageProcessor<?, ?> getMessageProcessor(String name, MessageProcessorParameters parameters) {
        return null;
    }
}
