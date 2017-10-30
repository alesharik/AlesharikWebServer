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

import com.alesharik.webserver.api.control.ControlSocketClientModule;
import com.alesharik.webserver.api.control.messaging.ControlSocketMessage;
import com.alesharik.webserver.api.control.messaging.ControlSocketMessageHandler;
import com.alesharik.webserver.api.control.messaging.ControlSocketServerConnection;
import com.alesharik.webserver.api.control.messaging.WireControlSocketMessage;
import com.alesharik.webserver.api.utils.crypto.StringCipher;
import com.alesharik.webserver.configuration.Layer;
import com.alesharik.webserver.configuration.Module;
import com.alesharik.webserver.control.dashboard.websocket.plugins.MenuDashboardWebSocketPlugin;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.module.security.ControlServerModule;
import com.alesharik.webserver.module.security.SecuredStoreAccessController;
import com.alesharik.webserver.module.security.SecuredStoreModule;
import com.alesharik.webserver.module.security.SecuredStoreModuleImpl;
import lombok.AllArgsConstructor;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

/**
 * Test class. Do not used in production
 */
@Deprecated
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
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void shutdownNow() {
        SecuredStoreModule securedStoreModule = new SecuredStoreModuleImpl();
        try {
            securedStoreModule.storeString(new SecuredStoreAccessController() {//Bind access controller to password name
                @Override
                public boolean grantAccess(Class<?> clazz) {
                    return true;//Return true classes, that allowed to access password, overwise false
                }

                @Override
                public SecretKey passwordKey() {
                    try {
                        return StringCipher.generateKey("somePrivateKey");//Return secret key for password encryption
                    } catch (InvalidKeySpecException | InvalidKeyException | UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, "testPassName");
            securedStoreModule.writeString("testPassName", "secretPass");//Write password "secretPass" into testPassName
            securedStoreModule.readString("testPassName");//Read testPassName password. Return "secretPass"
        } catch (IllegalAccessException | IOException | IllegalBlockSizeException | InvalidKeyException | InvalidKeySpecException | BadPaddingException e) {
            e.printStackTrace();
        }
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
        private static final long serialVersionUID = -9163081824991322648L;
        private final String test;

        public String test() {
            return test;
        }
    }

    @WireControlSocketMessage(TestMessage.class)
    @Prefixes("[Server]")
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
