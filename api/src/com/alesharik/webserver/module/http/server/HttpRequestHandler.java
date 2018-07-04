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

package com.alesharik.webserver.module.http.server;

import com.alesharik.webserver.module.http.addon.AddOn;
import com.alesharik.webserver.module.http.addon.AddOnSocketHandler;
import com.alesharik.webserver.module.http.addon.Message;
import com.alesharik.webserver.module.http.addon.MessageProcessor;
import com.alesharik.webserver.module.http.addon.MessageProcessorContext;
import com.alesharik.webserver.module.http.addon.MessageSender;
import com.alesharik.webserver.module.http.http.Request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * HttpRequestHandler handle all requests. Realisation must be named with {@link com.alesharik.webserver.api.name.Named} and
 * have public constructor with 1 parameter(<code>Set<{@link com.alesharik.webserver.module.http.bundle.HttpHandlerBundle}</code>)
 */
public interface HttpRequestHandler {
    void handleRequest(Request request, ExecutorPool executorPool, Sender sender);

    @Nullable
    AddOnSocketHandler getAddOnSocketHandler(@Nonnull Request request, @Nonnull ExecutorPool executorPool, @Nonnull AddOn addOn);

    void handleMessage(@Nonnull MessageProcessor messageProcessor, @Nonnull Message message, @Nonnull MessageProcessorContext context, @Nonnull MessageSender messageSender, @Nonnull ExecutorPool executorPool, @Nonnull AddOn addOn, @Nonnull Object sync);

    void handleMessageTask(@Nonnull Runnable task, @Nonnull ExecutorPool executorPool, @Nonnull AddOn addOn, @Nonnull Object sync);
}
