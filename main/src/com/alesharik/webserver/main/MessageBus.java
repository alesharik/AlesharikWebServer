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

package com.alesharik.webserver.main;

import com.alesharik.webserver.configuration.extension.ExtensionManager;
import com.alesharik.webserver.configuration.extension.message.Message;
import com.alesharik.webserver.configuration.extension.message.MessageManager;
import com.alesharik.webserver.configuration.extension.message.MessageSender;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.logger.level.Level;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;

@Prefixes({"[ExtensionManagement]", "[MessageBus]"})
@Level("main")
final class MessageBus extends Thread implements ExtensionManager.Listener {
    static {
        Logger.getLoggingLevelManager().createLoggingLevel("main");
    }

    private final BlockingQueue<Msg> send = new LinkedBlockingQueue<>();
    private final Sender anonymousSender = new Sender("");
    private final Map<String, MessageManager> managers = new HashMap<>();
    private final Object lock = new Object();

    public MessageBus() {
        super("ExtensionMessageBus");
        setDaemon(true);
        setPriority(Thread.MAX_PRIORITY - 2);
    }

    @Override
    public void run() {
        System.out.println("Starting main bus...");
        ExtensionManager.addListener(this);
        System.out.println("Initializing global message managers...");
        for(MessageManager messageManager : ExtensionManager.getGlobalMessageManagers())
            messageManager.init(anonymousSender);
        System.out.println("Main bus started");
        while(isAlive() && !isInterrupted()) {
            try {
                Msg msg = send.take();
                if(msg.isBroadcast()) {
                    for(Map.Entry<String, MessageManager> manager : managers.entrySet())
                        if(!manager.getKey().equals(msg.getSender()))
                            manager.getValue().listen(msg.getMessage(), msg.getSender());
                    for(MessageManager messageManager : ExtensionManager.getGlobalMessageManagers())
                        ForkJoinPool.commonPool().execute(() -> messageManager.listen(msg.getMessage(), msg.getSender()));
                } else {
                    if(managers.containsKey(msg.getReceiver()))
                        managers.get(msg.getReceiver()).listen(msg.getMessage(), msg.getSender());
                    else
                        System.out.println("Message manager with name " + msg.getReceiver() + " not found! Message: " + msg);
                }
                synchronized (lock) {
                    lock.notifyAll();
                }
            } catch (InterruptedException e) {
                System.err.println("Message bus got interrupted! Stopping...");
                return;
            }
        }
        ExtensionManager.removeListener(this);
    }

    public MessageSender getAnonymousSender() {
        return anonymousSender;
    }

    @TestOnly
    void waitForLoop() {
        synchronized (lock) {
            try {
                lock.wait(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void addManager(@Nonnull MessageManager messageManager, @Nonnull String name) {
        if(managers.containsKey(name))
            throw new IllegalArgumentException("Already contains manager " + name);
        managers.put(name, messageManager);
        messageManager.init(new Sender(name));
    }

    @Override
    public void messageManagerAdded(MessageManager messageManager) {
        messageManager.init(anonymousSender);
    }

    @RequiredArgsConstructor
    @ToString
    @Getter
    private static final class Msg {
        private final Message message;
        private final String sender;
        private final String receiver;
        private final boolean broadcast;
    }

    @RequiredArgsConstructor
    private final class Sender implements MessageSender {
        private final String owner;

        @Override
        public void broadcast(Message message) {
            send.add(new Msg(message, owner, "", true));
        }

        @Override
        public void send(Message message, String extensionName) {
            send.add(new Msg(message, owner, extensionName, false));
        }
    }
}
