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

package com.alesharik.webserver.main.platform;

import com.alesharik.webserver.api.documentation.test.PlatformTest;
import com.alesharik.webserver.configuration.config.lang.ConfigurationEndpoint;
import com.alesharik.webserver.configuration.config.lang.CustomEndpointSection;
import com.alesharik.webserver.configuration.config.lang.ScriptEndpointSection;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationPrimitive;
import com.alesharik.webserver.configuration.run.ExtensionManager;
import com.alesharik.webserver.configuration.run.message.Message;
import com.alesharik.webserver.configuration.run.message.MessageManager;
import com.alesharik.webserver.configuration.run.message.MessageSender;
import com.alesharik.webserver.main.Main;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static com.alesharik.webserver.main.platform.MockExtension.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Category(PlatformTest.class)
public class ExtensionPlatformTest {
    private static final TemporaryFolder folder = new TemporaryFolder();
    private File config;
    private File log;

    @BeforeClass
    public static void setup() throws Exception {
        folder.create();
        System.setProperty("extension-blacklist", "module");
    }

    @AfterClass
    public static void teardown() {
        folder.delete();
        System.getProperties().remove("extension-blacklist");
    }

    @Before
    public void setUp() throws Exception {
        config = folder.newFile("cfg.endpoint");
        assertTrue(config.setExecutable(true));
        InputStream res = new BufferedInputStream(ExtensionPlatformTest.class.getClassLoader().getResourceAsStream("com/alesharik/webserver/main/platform/cfg.endpoint"));
        OutputStream out = new FileOutputStream(config);
        byte[] buf = new byte[1024];
        int nRead;
        while((nRead = res.read(buf)) == 1024) {
            out.write(buf, 0, nRead);
        }
        out.write(buf, 0, nRead);
        res.close();
        out.flush();
        out.close();

        System.setProperty("config", config.getAbsolutePath());


        log = folder.newFile("log.log");
        LogDefineProvider.DEF = log.getAbsolutePath();

        reset(MOCK);
        reset(COMMAND_EXECUTOR_MOCK);
        reset(MESSAGE_MANAGER_MOCK);
        WATCHERS.clear();
        when(COMMAND_EXECUTOR_MOCK.getPredicate()).thenReturn(commandName -> commandName.startsWith("mock"));
    }

    @After
    public void tearDown() {
        assertTrue(log.delete());
        assertTrue(config.delete());
        System.getProperties().remove("config");
    }

    @Test
    public void startAndShutdown() {
        Main.main(new String[]{"-detach=in", "testing"});

        assertTrue(log.exists());

        ArgumentCaptor<ConfigurationEndpoint> endpointArgumentCaptor = ArgumentCaptor.forClass(ConfigurationEndpoint.class);
        verify(MOCK).load(endpointArgumentCaptor.capture(), any());
        ConfigurationEndpoint endpoint = endpointArgumentCaptor.getValue();

        CustomEndpointSection test = endpoint.getCustomSection("test");
        assertNotNull(test);

        CustomEndpointSection.UseDirective directive = test.getUseDirectives().get(0);
        assertEquals("qw", directive.getName());
        assertEquals("a", ((ConfigurationPrimitive.String) directive.getConfiguration().getElement("a")).value());
        CustomEndpointSection.CustomProperty customProperty = directive.getCustomProperties().get(0);
        assertEquals("a", customProperty.getName());
        CustomEndpointSection.UseCommand command = customProperty.getUseCommands().get(0);
        assertEquals("a", command.getReferent());
        assertEquals("on b", command.getArg());

        {
            ArgumentCaptor<ScriptEndpointSection.Command> commandArgumentCaptor = ArgumentCaptor.forClass(ScriptEndpointSection.Command.class);
            verify(COMMAND_EXECUTOR_MOCK, times(3)).execute(commandArgumentCaptor.capture());
            assertEquals("mock_pre-init", commandArgumentCaptor.getAllValues().get(0).getName());
            assertNull(commandArgumentCaptor.getAllValues().get(0).getArg());
            assertEquals("mock_init", commandArgumentCaptor.getAllValues().get(1).getName());
            assertNull(commandArgumentCaptor.getAllValues().get(1).getArg());
            assertEquals("mock_post-init", commandArgumentCaptor.getAllValues().get(2).getName());
            assertNull(commandArgumentCaptor.getAllValues().get(2).getArg());
        }

        verify(MOCK).start();
        reset(COMMAND_EXECUTOR_MOCK);
        when(COMMAND_EXECUTOR_MOCK.getPredicate()).thenReturn(commandName -> commandName.startsWith("mock"));

        Main.shutdown();

        {
            ArgumentCaptor<ScriptEndpointSection.Command> commandArgumentCaptor = ArgumentCaptor.forClass(ScriptEndpointSection.Command.class);
            verify(COMMAND_EXECUTOR_MOCK, times(3)).execute(commandArgumentCaptor.capture());
            assertEquals("mock_pre-shutdown", commandArgumentCaptor.getAllValues().get(0).getName());
            assertNull(commandArgumentCaptor.getAllValues().get(0).getArg());
            assertEquals("mock_shutdown", commandArgumentCaptor.getAllValues().get(1).getName());
            assertNull(commandArgumentCaptor.getAllValues().get(1).getArg());
            assertEquals("mock_post-shutdown", commandArgumentCaptor.getAllValues().get(2).getName());
            assertNull(commandArgumentCaptor.getAllValues().get(2).getArg());
        }

        verify(MOCK).shutdown();
    }

    @Test
    public void messaging() throws InterruptedException {
        Main.main(new String[]{"-detach=in", "testing"});

        assertTrue(log.exists());

        ArgumentCaptor<MessageSender> senderArgumentCaptor = ArgumentCaptor.forClass(MessageSender.class);
        verify(MESSAGE_MANAGER_MOCK).init(senderArgumentCaptor.capture());
        MessageSender sender = senderArgumentCaptor.getValue();

        MessageManager messageManager = mock(MessageManager.class);
        ExtensionManager.registerMessageManager(messageManager);
        ArgumentCaptor<MessageSender> senderArgumentCaptor1 = ArgumentCaptor.forClass(MessageSender.class);
        verify(messageManager).init(senderArgumentCaptor1.capture());
        MessageSender senderGlobal = senderArgumentCaptor1.getValue();

        Message message1 = mock(Message.class);
        senderGlobal.send(message1, "mock");
        Thread.sleep(500);
        verify(MESSAGE_MANAGER_MOCK).listen(eq(message1), anyString());

        Message message2 = mock(Message.class);
        sender.broadcast(message2);
        Thread.sleep(500);
        verify(messageManager).listen(message2, "mock");

        ExtensionManager.unregisterMessageManager(messageManager);
        Main.shutdown();
    }

    @Test
    public void startAndShutdownNow() {
        Main.main(new String[]{"-detach=in", "testing"});

        assertTrue(log.exists());

        verify(MOCK).start();
        reset(COMMAND_EXECUTOR_MOCK);

        Main.shutdownNow();

        verify(COMMAND_EXECUTOR_MOCK, never()).execute(any());

        verify(MOCK).shutdownNow();
    }
}
