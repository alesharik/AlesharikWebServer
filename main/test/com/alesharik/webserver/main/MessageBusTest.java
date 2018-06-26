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
import lombok.SneakyThrows;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class MessageBusTest {
    private static MessageBus bus = new MessageBus();
    private static MessageManager manager;

    @BeforeClass
    public static void setUp() throws InterruptedException {
        manager = mock(MessageManager.class);
        ExtensionManager.registerMessageManager(manager);
        bus.start();
        bus.getAnonymousSender().send(mock(Message.class), "a");
        bus.waitForLoop();
        Thread.sleep(10);//Give thread time
        verify(manager).init(bus.getAnonymousSender());
        MessageManager messageManager = mock(MessageManager.class);
        ExtensionManager.registerMessageManager(messageManager);
        verify(messageManager).init(bus.getAnonymousSender());
    }

    @AfterClass
    public static void tearDown() {
        bus.interrupt();
    }

    @Before
    public void setup() {
        reset(manager);
    }

    @Test
    public void broadcast() {
        MessageManager messageManager = mock(MessageManager.class);
        bus.addManager(messageManager, "a");
        MessageManager messageManager1 = mock(MessageManager.class);
        bus.addManager(messageManager1, "b");
        ArgumentCaptor<MessageSender> captor = ArgumentCaptor.forClass(MessageSender.class);
        verify(messageManager).init(captor.capture());
        MessageSender sender = captor.getValue();

        Message mock = mock(Message.class);
        sender.broadcast(mock);
        waitForSend();

        verify(messageManager, never()).listen(mock, "a");
        verify(messageManager1, times(1)).listen(eq(mock), eq("a"));
        verify(manager, times(1)).listen(eq(mock), eq("a"));
    }

    @Test
    public void send() {
        MessageManager messageManager = mock(MessageManager.class);
        bus.addManager(messageManager, "c");
        MessageManager messageManager1 = mock(MessageManager.class);
        bus.addManager(messageManager1, "d");
        ArgumentCaptor<MessageSender> captor = ArgumentCaptor.forClass(MessageSender.class);
        verify(messageManager).init(captor.capture());
        MessageSender sender = captor.getValue();

        Message message = mock(Message.class);
        sender.send(message, "d");
        waitForSend();

        verify(messageManager, never()).listen(eq(message), anyString());
        verify(messageManager1, times(1)).listen(eq(message), eq("c"));
        verify(manager, never()).listen(eq(message), anyString());
    }

    @Test
    public void sendToNoOne() {
        MessageManager messageManager = mock(MessageManager.class);
        bus.addManager(messageManager, "e");
        MessageManager messageManager1 = mock(MessageManager.class);
        bus.addManager(messageManager1, "f");
        ArgumentCaptor<MessageSender> captor = ArgumentCaptor.forClass(MessageSender.class);
        verify(messageManager).init(captor.capture());
        MessageSender sender = captor.getValue();

        Message message = mock(Message.class);
        sender.send(message, "qwerty");
        waitForSend();

        verify(messageManager, never()).listen(eq(message), anyString());
        verify(messageManager1, never()).listen(eq(message), anyString());
        verify(manager, never()).listen(eq(message), anyString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerOneManagerTwice() {
        bus.addManager(mock(MessageManager.class), "qwe");
        bus.addManager(mock(MessageManager.class), "qwe");
    }

    @SneakyThrows
    private void waitForSend() {
        bus.waitForLoop();
        ForkJoinPool.commonPool().awaitQuiescence(10000, TimeUnit.MILLISECONDS);
        Thread.sleep(100);
        ForkJoinPool.commonPool().invoke(new ForkJoinTask<Object>() {
            private static final long serialVersionUID = 7182502388918002183L;

            @Override
            public Object getRawResult() {
                return "";
            }

            @Override
            protected void setRawResult(Object o) {

            }

            @Override
            protected boolean exec() {
                return true;
            }
        });
    }
}