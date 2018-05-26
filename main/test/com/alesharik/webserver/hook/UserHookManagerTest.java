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

package com.alesharik.webserver.hook;

import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import org.glassfish.grizzly.utils.Charsets;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

import static com.alesharik.webserver.test.TestUtils.assertUtilityClass;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

public class UserHookManagerTest {
    @Before
    public void setUp() throws Exception {
        UserHookManager.cleanHooks();
        UserHookManager.hookFactories.clear();
    }

    @Test
    public void testUtility() throws Exception {
        assertUtilityClass(UserHookManager.class);
    }

    @Test
    public void testListen() throws Exception {
        assertTrue(UserHookManager.hookFactories.isEmpty());

        UserHookManager.listen(TestFactory.class);
        assertTrue(UserHookManager.hookFactories.containsKey("test"));
    }

    @Test
    public void testListenOverwriteProtection() throws Exception {
        assertTrue(UserHookManager.hookFactories.isEmpty());

        for(int i = 0; i < 1000; i++) {
            UserHookManager.listen(TestFactory.class);
        }

        assertTrue(UserHookManager.hookFactories.containsKey("test"));
        assertEquals(1, UserHookManager.hookFactories.size());
    }

    @Test(expected = ConfigurationParseError.class)
    public void testCreateHookWithIllegalFactory() throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new ByteArrayInputStream(("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<hook>\n" +
                "                <name>test</name>\n" +
                "                <factory>javascript</factory>\n" +
                "                <configuration>\n" +
                "                    <java>true</java>\n" +
                "                    <isolated>false</isolated>\n" +
                "                    <code>\n" +
                "                        function run(sender, args) {\n" +
                "                            print(\"test\");\n" +
                "                        }\n" +
                "                    </code>\n" +
                "                </configuration>\n" +
                "            </hook>").getBytes(Charsets.UTF8_CHARSET)));
        UserHookManager.parseHook(doc.getDocumentElement());
    }

    @Test
    public void testCreateHook() throws Exception {
        UserHookManager.listen(TestFactory.class);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new ByteArrayInputStream(("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<hook>\n" +
                "                <name>test-hook</name>\n" +
                "                <factory>test</factory>\n" +
                "                <configuration>\n" +
                "                </configuration>\n" +
                "            </hook>").getBytes(Charsets.UTF8_CHARSET)));
        UserHookManager.parseHook(doc.getDocumentElement());

        Hook hook = HookManager.getHookForName("test-hook");
        assertNotNull(hook);
        assertTrue(mockingDetails(hook).isMock());

        UserHookManager.cleanHooks();
        assertNull(HookManager.getHookForName("test-hook"));
    }

    private static final class TestFactory implements HookFactory {

        @Nonnull
        @Override
        public Hook create(@Nonnull Element config, @Nonnull String name) {
            return mock(Hook.class);
        }

        @Nonnull
        @Override
        public String getName() {
            return "test";
        }
    }
}