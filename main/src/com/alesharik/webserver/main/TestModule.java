package com.alesharik.webserver.main;

import com.alesharik.webserver.api.control.ControlSocketClientModule;
import com.alesharik.webserver.api.control.messaging.ControlSocketMessage;
import com.alesharik.webserver.api.control.messaging.ControlSocketMessageHandler;
import com.alesharik.webserver.api.control.messaging.ControlSocketServerConnection;
import com.alesharik.webserver.api.control.messaging.WireControlSocketMessage;
import com.alesharik.webserver.configuration.Layer;
import com.alesharik.webserver.configuration.Module;
import com.alesharik.webserver.control.dashboard.websocket.plugins.MenuDashboardWebSocketPlugin;
import com.alesharik.webserver.logger.Prefix;
import com.alesharik.webserver.module.server.ControlServerModule;
import lombok.AllArgsConstructor;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public class TestModule implements Module {
    private ControlSocketClientModule clientModule;

    @Override
    public void parse(@Nullable Element configNode) {
//        clientModule = XmlHelper.getControlSocketClient("controlSocketClient", configNode, true);
        ControlServerModule controlServerModule = Main.getControlServer("controlServer", configNode, true);
        controlServerModule.addDashboardWebSocketPluginListener("menu", MenuDashboardWebSocketPlugin.class, System.out::println);
    }

    @Override
    public void reload(@Nullable Element configNode) {

    }

    @Override
    public void start() {
//        try {
//            ControlSocketClientConnection connection = clientModule.newConnection("0.0.0.0", 1400, new ControlSocketClientConnection.Authenticator() {
//                @Override
//                public String getPassword() {
//                    return "admin";
//                }
//
//                @Override
//                public String getLogin() {
//                    return "admin";
//                }
//            });
//            connection.addListener(new ControlSocketClientConnection.Listener() {
//                @Override
//                public boolean canListen(Class<?> messageClazz) {
//                    return TestMessage.class.isAssignableFrom(messageClazz);
//                }
//
//                @Override
//                public void listen(ControlSocketMessage message) {
//                    System.out.println(((TestMessage) message).test());
//                }
//            });
//            connection.awaitConnection();
//            connection.sendMessage(new TestMessage("test"));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void shutdownNow() {

    }

    @Nonnull
    @Override
    public String getName() {
        return "test";
    }

    @Nullable
    @Override
    public Layer getMainLayer() {
        return null;
    }

    @AllArgsConstructor
    private static final class TestMessage implements ControlSocketMessage {
        private final String test;

        public String test() {
            return test;
        }
    }

    @WireControlSocketMessage(TestMessage.class)
    @Prefix("[Server]")
    private static final class TestMessageHandler implements ControlSocketMessageHandler<TestMessage> {
        public TestMessageHandler() {
        }

        @Override
        public void handleMessage(TestMessage message, ControlSocketServerConnection connection) {
            System.out.println(message.test());
            try {
                connection.sendMessage(new TestMessage("asd"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
