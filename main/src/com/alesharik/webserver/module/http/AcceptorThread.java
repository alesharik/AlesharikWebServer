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

package com.alesharik.webserver.module.http;

import com.alesharik.webserver.module.http.server.ExecutorPool;
import com.alesharik.webserver.module.http.server.socket.ServerSocketWrapper;
import sun.misc.Cleaner;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Acceptor thread accepts new connections and publish them into {@link ExecutorPool},
 * You is free to use this class in your projects, but it isn't designed as public and it's API can be changed
 */
public final class AcceptorThread extends Thread {
    private final ExecutorPool executorPool;
    private final Selector selector;
    private volatile boolean isRunning;

    public AcceptorThread(ThreadGroup group, ExecutorPool executorPool) {
        super(group, "AcceptorThread");
        this.executorPool = executorPool;
        try {
            this.selector = Selector.open();
            Cleaner.create(this, () -> {
                try {
                    selector.close();
                } catch (IOException ignored) {
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Register server socket to internal selector
     *
     * @param wrapper server socket wrapper
     */
    public void handle(ServerSocketWrapper wrapper) {
        wrapper.registerSelector(selector);
        selector.wakeup();
    }

    @Override
    public void run() {
        while(isRunning) {
            try {
                int ops = selector.select();
                if(ops == 0)
                    continue;

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while(iterator.hasNext()) {
                    SelectionKey next = iterator.next();
                    if(!next.isValid()) {
                        iterator.remove();
                        continue;
                    }

                    if(next.isAcceptable()) {
                        ServerSocketWrapper attachment = (ServerSocketWrapper) next.attachment();
                        SocketChannel socket = attachment.getChannel().accept();
                        if(socket == null)
                            continue;
                        if(!socket.finishConnect()) {
                            System.err.println("Socket " + socket + " didn't finish connectiong in time!");
                            continue;
                        }

                        SelectableChannel socketChannel = socket.configureBlocking(false);
                        executorPool.selectSocket(socketChannel, socket, attachment.getSocketManager());
                    }
                }
                keys.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void start() {
        isRunning = true;
        super.start();
    }

    public void shutdownNow() {
        isRunning = false;
        selector.wakeup();
    }

    public void shutdown() {
        isRunning = false;
        selector.wakeup();
    }
}
