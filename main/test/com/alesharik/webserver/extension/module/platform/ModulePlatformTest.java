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

package com.alesharik.webserver.extension.module.platform;

import com.alesharik.webserver.api.documentation.test.PlatformTest;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.main.Main;
import com.alesharik.webserver.main.platform.ExtensionPlatformTest;
import com.alesharik.webserver.main.platform.LogDefineProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@Category(PlatformTest.class)
public class ModulePlatformTest {
    private static final TemporaryFolder folder = new TemporaryFolder();
    private File config;

    @Before
    public void setUp() throws Exception {
        Logger.reset();
        folder.create();
        {
            config = folder.newFile("cfg.endpoint");
            assertTrue(config.setExecutable(true));
            InputStream res = new BufferedInputStream(ExtensionPlatformTest.class.getClassLoader().getResourceAsStream("com/alesharik/webserver/extension/module/cfg.endpoint"));
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
        }
        {
            File m = folder.newFile("m.module");
            assertTrue(m.setExecutable(true));
            InputStream res = new BufferedInputStream(ExtensionPlatformTest.class.getClassLoader().getResourceAsStream("com/alesharik/webserver/extension/module/m.module"));
            OutputStream out = new FileOutputStream(m);
            byte[] buf = new byte[1024];
            int nRead;
            while((nRead = res.read(buf)) == 1024) {
                out.write(buf, 0, nRead);
            }
            out.write(buf, 0, nRead);
            res.close();
            out.flush();
            out.close();
        }
        System.setProperty("config", config.getAbsolutePath());


        File log = folder.newFile("log.log");
        LogDefineProvider.DEF = log.getAbsolutePath();

        Main.main(new String[]{"-detach=in", "testing"});
    }

    @After
    public void tearDown() throws Exception {
        folder.delete();
        reset(MockModule1.LAYER_MOCK);
        reset(MockModule1.SUB_MODULE_MOCK);
        reset(MockModule1.MOCK);
        reset(MockModule2.MOCK);
        Logger.reset();
    }

    @Test
    public void startAndShutdown() {
        assertEquals("test", MockModule1.A);
        verify(MockModule1.MOCK, times(1)).start();
        verify(MockModule1.LAYER_MOCK, times(1)).start();
        verify(MockModule1.SUB_MODULE_MOCK, times(1)).start();

        assertEquals(1, MockModule2.CFG.a);
        assertEquals("asd", MockModule2.CFG.b);
        verify(MockModule2.MOCK, times(1)).start();

        Main.shutdown();

        verify(MockModule1.SUB_MODULE_MOCK, times(1)).shutdown();
        verify(MockModule1.LAYER_MOCK, times(1)).shutdown();
        verify(MockModule1.MOCK, times(1)).shutdown();
        verify(MockModule2.MOCK, times(1)).shutdown();
    }

    @Test
    public void emergencyShutdown() {
        assertEquals("test", MockModule1.A);
        verify(MockModule1.MOCK, times(1)).start();
        verify(MockModule1.LAYER_MOCK, times(1)).start();
        verify(MockModule1.SUB_MODULE_MOCK, times(1)).start();

        assertEquals(1, MockModule2.CFG.a);
        assertEquals("asd", MockModule2.CFG.b);
        verify(MockModule2.MOCK, times(1)).start();

        Main.shutdownNow();

        verify(MockModule1.SUB_MODULE_MOCK, times(1)).shutdownNow();
        verify(MockModule1.LAYER_MOCK, times(1)).shutdownNow();
        verify(MockModule1.MOCK, times(1)).shutdownNow();
        verify(MockModule2.MOCK, times(1)).shutdownNow();
    }//FIXME reload tests
}
