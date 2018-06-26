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

import com.alesharik.webserver.configuration.extension.DirectoryWatcher;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.logger.level.Level;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.nio.file.StandardWatchEventKinds.*;

@Level("configuration-runner")
@Prefixes("[FileWatcher]")
final class FileWatcherThread extends Thread {//TODO move to utils
    private final Object initSync = new Object();
    private volatile boolean init = false;
    private final Object loopSync = new Object();

    @Nullable
    private final Path config;
    private final ConfigListener listener;
    private final List<DirectoryWatcher> watchers = new CopyOnWriteArrayList<>();

    private volatile boolean running = true;

    private WatchService service;

    public FileWatcherThread(@Nullable File config, @Nonnull ConfigListener listener) {
        super("FileWatcher");
        this.listener = listener;
        setDaemon(true);
        setPriority(Thread.MIN_PRIORITY + 2);
        this.config = config == null ? null : config.toPath().toAbsolutePath();
    }

    @Override
    public void run() {
        try {
            init();
            synchronized (initSync) {
                init = true;
                initSync.notifyAll();
            }

            while(running && isAlive() && !isInterrupted())
                loop();
        } catch (IOException e) {
            System.err.println("Exception in file watcher thread!");
            e.printStackTrace();
        } catch (InterruptedException ignored) {
            System.err.println("Received interrupt! Stopping...");
        }

        try {
            end();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @TestOnly
    void waitForInit() throws InterruptedException {
        synchronized (initSync) {
            if(init)
                return;
            initSync.wait();
        }
    }

    @TestOnly
    void waitForLoop(Callable<?> runnable) throws Exception {
        synchronized (loopSync) {
            runnable.call();
            loopSync.wait();
        }
    }

    private void init() throws IOException {
        System.out.println("Setting up file watcher...");
        service = FileSystems.getDefault().newWatchService();

        if(config != null)
            config.getParent().register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        for(DirectoryWatcher watcher : watchers)
            watcher.toWatch().register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }

    private void loop() throws InterruptedException, IOException {
        while(running) {
            WatchKey key = service.take();

            for(WatchEvent<?> watchEvent : key.pollEvents()) {
                if(watchEvent.kind() == OVERFLOW) {
                    System.err.println("Received OVERFLOW");
                    continue;
                }
                //noinspection unchecked
                WatchEvent<Path> event = (WatchEvent<Path>) watchEvent;
                Path real = ((Path) key.watchable()).resolve(event.context());

                if(Files.isDirectory(real)) {
                    if(event.kind() == ENTRY_CREATE && directoryWatchable(real)) {
                        System.out.println("Got directory " + real);
                        real.register(service, ENTRY_MODIFY, ENTRY_DELETE, ENTRY_CREATE);
                    }
                } else {
                    if(config != null && real.getParent().equals(config.getParent())) {
                        if(real.equals(config)) {
                            if(watchEvent.kind() == ENTRY_MODIFY)
                                listener.configUpdated(real.toFile());
                            else if(watchEvent.kind() == ENTRY_DELETE)
                                listener.configDeleted(real.toFile());
                        }
                    }
                    for(DirectoryWatcher watcher : watchers) {
                        if(real.startsWith(watcher.toWatch()))
                            watcher.fileChanged(real, event.kind());
                    }
                }
            }

            boolean valid = key.reset();
            if(!valid)
                if(Files.exists((Path) key.watchable()))
                    System.err.println("Key is not valid!");

            synchronized (loopSync) {
                loopSync.notifyAll();
            }
        }
    }

    private boolean directoryWatchable(Path dir) {
        for(DirectoryWatcher watcher : watchers) {
            if(dir.startsWith(watcher.toWatch()))
                return true;
        }
        return false;
    }

    private void end() throws IOException {
        service.close();
    }

    public void shutdown() {
        running = false;
        interrupt();
    }

    public void addDirectoryWatcher(DirectoryWatcher watcher) {
        watchers.add(watcher);
        if(service != null) {
            try {
                watcher.toWatch().register(service, ENTRY_DELETE, ENTRY_CREATE, ENTRY_MODIFY);
            } catch (IOException e) {
                System.err.println("Failed to add directory watcher for folder " + watcher.toWatch());
                e.printStackTrace();
            }
        }
    }

    public interface ConfigListener {
        void configUpdated(@Nonnull File config);

        void configDeleted(@Nonnull File config);
    }
}
