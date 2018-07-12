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

package com.alesharik.webserver.router;

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This is a router client. The clients connectAndSend to router server.
 * This class used only in microservices.
 *
 * @see RouterServer
 */
@Prefixes("[RouterClient]")
public final class Router {
    private static final Executor EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r);
        thread.setUncaughtExceptionHandler((t, e) -> Logger.log(e));
        return thread;
    });

    private final RouterClient client;

    public Router(int port, String host) {
        client = new RouterClient(port, host);
    }

    public void start() {
        client.start();
    }

    public void shutdown() {
        client.shutdown();
    }

    public Future<String> getIpForMicroservice(String name) throws IOException {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return client.send("get:" + name).get();
            } catch (InterruptedException | ExecutionException e) {
                Logger.log(e);
            }
            return "";
        }).thenApply(Router::checkMsg);
    }

    public void addNewMicroserviceServer(String[] microservices) throws IOException {
        Objects.requireNonNull(microservices);

        EXECUTOR.execute(() -> {
            try {
                checkMsg(client.send("add:" + Arrays.toString(microservices)).get());
            } catch (InterruptedException | ExecutionException e) {
                Logger.log(e);
            }
        });
    }

    public void removeMicroserviceServer() throws IOException {
        EXECUTOR.execute(() -> {
            try {
                checkMsg(client.send("remove").get());
            } catch (InterruptedException | ExecutionException e) {
                Logger.log(e);
            }
        });
    }

    private static String checkMsg(String msg) {
        Logger.log(msg);
        switch (msg) {
            case RouterServer.OK:
                return msg;
            case RouterServer.NOT_FOUND:
                throw new RuntimeException("Microservice server not found!");
            case RouterServer.INTERNAL_ERROR:
                throw new RuntimeException("Internal server error on server side!");
            default:
                throw new RuntimeException("Response from service is incorrect!");
        }
    }
}

