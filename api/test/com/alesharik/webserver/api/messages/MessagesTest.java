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

package com.alesharik.webserver.api.messages;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

public class MessagesTest {
    private static final String DEFAULT = "default";
    private static final MessageTranslator<TestMessage> translator = message -> message.setString("as");
    private static final MessageTranslator<TestMessage> translator1 = message -> message.setString("as");
    private static final MessageListener<TestMessage> listener = message -> System.out.println(message.getString());
    private static final MessageListener<TestMessage> listener1 = message -> System.out.println(message.getString());


    private static final MessageTranslator<TestMessage> translatorNotReg = message -> message.setString("as");
    private static final MessageListener<TestMessage> listenerNotReg = message -> System.out.println(message.getString());

    private static final MessageHandler handler = new MessageHandler() {
        @Subscribe(name = "test", subName = "msg")
        public void msg(TestMessage testMessage) {
            System.out.println(testMessage.getString());
        }
    };

    @BeforeClass
    public static void setUp() throws Exception {
        Messages.registerNewMessage("test", new AbstractMessageFactory<Message>() {
            @Override
            public String getName() {
                return "msg";
            }

            @Override
            public Message newMessage() {
                return new TestMessage();
            }
        });
        Messages.registerNewTranslator("test", "msg", translator1);
        Messages.registerNewListener("test", "msg", listener1);

        Messages.registerNewMessage("asd", new AbstractMessageFactory<Message>() {
            @Override
            public String getName() {
                return "sdf";
            }

            @Override
            public Message newMessage() {
                return new BoolChanger();
            }
        });
    }

    @Test
    public void registerNewTranslator() throws Exception {
        Messages.registerNewTranslator("test", "msg", translator);
    }

    @Test
    public void unregisterTranslator() throws Exception {
        Messages.unregisterTranslator("test", "msg", translator1);
    }

    @Test
    public void registerNewListener() throws Exception {
        Messages.registerNewListener("test", "msg", listener);
    }

    @Test
    public void unregisterListener() throws Exception {
        Messages.unregisterListener("test", "msg", listener1);
    }

    @Test
    public void publishMessage() throws Exception {
        Messages.publishMessage("test", "msg");
    }

    @Test
    public void registerNewHandler() throws Exception {
        Messages.registerNewHandler(handler);
    }

    @Test
    public void unregisterHandler() throws Exception {
//        Messages.unregisterHandler(handler);
    }

    @Test
    public void registerNewMessage() throws Exception {
        Messages.registerNewMessage("asd", new AbstractMessageFactory<Message>() {
            @Override
            public String getName() {
                return "asd";
            }

            @Override
            public Message newMessage() {
                return new Message();
            }
        });
    }


    @Test(expected = IllegalStateException.class)
    public void registerNewTranslatorIllegalState() throws Exception {
        Messages.registerNewTranslator("none", "msg", translator);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unregisterTranslatorIllegalArgument() throws Exception {
        Messages.unregisterTranslator("test", "msg", translatorNotReg);
    }

    @Test(expected = IllegalStateException.class)
    public void unregisterTranslatorIllegalState() throws Exception {
        Messages.unregisterTranslator("none", "msg", translator1);
    }

    @Test(expected = IllegalStateException.class)
    public void registerNewListenerIllegalState() throws Exception {
        Messages.registerNewListener("none", "msg", listener);
    }

    @Test(expected = IllegalStateException.class)
    public void unregisterListenerIllegalState() throws Exception {
        Messages.unregisterListener("none", "msg", listener1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unregisterListenerIllegalArgument() throws Exception {
        Messages.unregisterListener("test", "msg", listenerNotReg);
    }

    @Test(expected = IllegalStateException.class)
    public void publishMessageIllegalState() throws Exception {
        Messages.publishMessage("none", "msg");
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerNewMessageIllegalArgument() throws Exception {
        Messages.registerNewMessage("test", new AbstractMessageFactory<TestMessage>() {
            @Override
            public String getName() {
                return "msg";
            }

            @Override
            public TestMessage newMessage() {
                return new TestMessage();
            }
        });
    }

    @Test
    public void messageCycle() throws InterruptedException {
        Messages.registerNewMessage("tests", new AbstractMessageFactory<Message>() {
            @Override
            public String getName() {
                return "BoolChanger";
            }

            @Override
            public Message newMessage() {
                return new BoolChanger();
            }
        });

        AtomicBoolean isListenerOk = new AtomicBoolean(false);
        AtomicBoolean isHandlerOk = new AtomicBoolean(false);

        MessageTranslator<BoolChanger> change = BoolChanger::change;
        Messages.registerNewTranslator("tests", "BoolChanger", change);
        MessageListener<BoolChanger> boolChangerMessageListener = message -> isListenerOk.set(message.get());
        Messages.registerNewListener("tests", "BoolChanger", boolChangerMessageListener);
        MessageHandler handler = new MessageHandler() {
            @Subscribe(name = "asd", subName = "sdf")
            public void none() {
            }

            @Subscribe(name = "tests", subName = "BoolChanger")
            public void ok(BoolChanger boolChanger) {
                isHandlerOk.set(boolChanger.get());
            }
        };
        Messages.registerNewHandler(handler);
        Messages.publishMessage("tests", "BoolChanger");
        Thread.sleep(5);
//        while(!isHandlerOk.get()) {
//            Thread.sleep(1);
//        }
        assertTrue(isHandlerOk.get());
        assertTrue(isListenerOk.get());

        Messages.unregisterHandler(handler);
        Messages.unregisterTranslator("tests", "BoolChanger", change);
        Messages.unregisterListener("tests", "BoolChanger", boolChangerMessageListener);
    }

    private static final class BoolChanger extends Message {
        private AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        public BoolChanger() {
        }

        public void change() {
            atomicBoolean.set(true);
        }

        public boolean get() {
            return atomicBoolean.get();
        }
    }

    private static final class TestMessage extends Message {
        private String string;

        public TestMessage(String string) {
            this.string = string;
        }

        public TestMessage() {
            this.string = DEFAULT;
        }

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }
    }

}