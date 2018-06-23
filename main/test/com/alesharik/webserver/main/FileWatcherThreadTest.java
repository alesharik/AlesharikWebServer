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

import com.alesharik.webserver.configuration.run.DirectoryWatcher;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardWatchEventKinds;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class FileWatcherThreadTest {
    private static TemporaryFolder folder;
    private static File modules;
    private static File module1;
    private static File config;
    private static FileWatcherThread thread;
    private static FileWatcherThread.ConfigListener listener;
    private static DirectoryWatcher watcher;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(1);

    @BeforeClass
    public static void setup() throws IOException, InterruptedException {
        listener = mock(FileWatcherThread.ConfigListener.class);
        folder = new TemporaryFolder();
        folder.create();
        modules = folder.newFolder();
        module1 = new File(modules + "/a");
        assertTrue(module1.createNewFile());
        config = folder.newFile();
        thread = new FileWatcherThread(config, listener);
        watcher = mock(DirectoryWatcher.class);
        thread.start();
        thread.waitForInit();
        when(watcher.toWatch()).thenReturn(modules.getAbsoluteFile().toPath());
        thread.addDirectoryWatcher(watcher);
    }

    @Before
    public void before() throws IOException {
        if(!module1.exists())
            assertTrue(module1.createNewFile());
        if(!config.exists())
            assertTrue(config.createNewFile());
        reset(listener);
    }

    @AfterClass
    public static void after() {
        thread.shutdown();
        folder.delete();
    }

    @Test
    public void updateConfig() throws Exception {
        thread.waitForLoop(() -> {
            Files.write(config.toPath(), "test".getBytes());
            return null;
        });//wait for thread
        verify(listener, atLeastOnce()).configUpdated(config);
    }

    @Test
    public void deleteConfig() throws Exception {
        thread.waitForLoop(() -> {
            assertTrue(config.delete());
            return null;
        });//wait for thread
        verify(listener, times(1)).configDeleted(config);
    }

    @Test
    public void watchAdded() throws Exception {
        File a = new File(modules + "/b");
        thread.waitForLoop(() -> {
            assertTrue(a.createNewFile());
            return null;
        });
        verify(watcher, times(1)).fileChanged(a.toPath(), StandardWatchEventKinds.ENTRY_CREATE);
    }

    @Test
    public void watchUpdated() throws Exception {
        thread.waitForLoop(() -> {
            Files.write(module1.toPath(), "test".getBytes());
            return null;
        });
        verify(watcher, atLeastOnce()).fileChanged(module1.toPath(), StandardWatchEventKinds.ENTRY_MODIFY);
    }

    @Test
    public void watchDeleted() throws Exception {
        thread.waitForLoop(() -> {
            assertTrue(module1.delete());
            return null;
        });
        verify(watcher, times(1)).fileChanged(module1.toPath(), StandardWatchEventKinds.ENTRY_DELETE);
    }

    @Test
    public void folderSyncWatch() throws Exception {
        File f = new File(modules + "/qwerty");
        thread.waitForLoop(() -> {
            assertTrue(f.mkdir());
            return null;
        });//wait for register
        File file = new File(f + "/a");
        thread.waitForLoop(() -> {
            assertTrue(file.createNewFile());
            return null;
        });
        verify(watcher, times(1)).fileChanged(file.toPath(), StandardWatchEventKinds.ENTRY_CREATE);
    }
}