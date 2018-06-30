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

import com.alesharik.webserver.configuration.config.lang.ConfigurationEndpoint;
import com.alesharik.webserver.configuration.config.lang.ScriptEndpointSection;
import com.alesharik.webserver.configuration.extension.Extension;
import com.alesharik.webserver.extension.module.meta.ScriptElementConverter;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.logger.level.Level;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

@RequiredArgsConstructor
@Level("configuration-runner")
@Prefixes("[ExtensionPool]")
final class ExtensionPool {
    private final Map<String, Extension> extensions;
    private final Map<String, Worker> workers = new HashMap<>();
    private final ThreadGroup threadGroup = new ThreadGroup("ExtensionThreadPool");

    public void start() {
        System.out.println("Starting extension pool...");
        for(Map.Entry<String, Extension> e : extensions.entrySet()) {
            Worker worker = new Worker(e.getValue(), e.getKey());
            workers.put(e.getKey(), worker);
            e.getValue().listenPoolThread(worker);
            worker.start();
        }
        System.out.println("Extension pool started");
    }

    public void addTask(String extensionName, Runnable task) {
        if(workers.containsKey(extensionName))
            workers.get(extensionName).execute(task);
    }

    public void load(ConfigurationEndpoint configurationEndpoint, ScriptElementConverter converter) {
        for(Worker worker : workers.values())
            worker.execute(() -> worker.extension.load(configurationEndpoint, converter));
    }

    public void executeCommand(String extensionName, ScriptEndpointSection.Command command) {
        if(workers.containsKey(extensionName)) {
            Worker worker = workers.get(extensionName);
            worker.execute(() -> worker.extension.getCommandExecutor().execute(command));
        }
    }

    public void reload(ConfigurationEndpoint last, ConfigurationEndpoint current, ScriptElementConverter converter) {
        for(Worker worker : workers.values())
            worker.execute(() -> worker.extension.reloadConfig(last, current, converter));
    }

    public void execStart() {
        for(Worker worker : workers.values())
            worker.execute(worker.extension::start);
    }

    public void shutdownExtensions() {
        for(Worker worker : workers.values())
            worker.execute(worker.extension::shutdown);
    }

    public void shutdownPool() {
        for(Worker worker : workers.values())
            worker.interrupt();
        joinAll();
    }

    public void shutdownNow() {
        for(Worker worker : workers.values()) {
            worker.execute(worker.extension::shutdownNow);
            worker.interrupt();
        }
        joinAll();
    }

    public void waitQuiescence() {
        for(Worker worker : workers.values()) {
            worker.waitForExec();
        }
    }

    private void joinAll() {
        for(Worker worker : workers.values()) {
            try {
                worker.join();
                worker.helpShutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    @Level("configuration-runner")
    @Prefixes({"[ExtensionPool]", "[Worker]"})
    private final class Worker extends Thread implements Executor {
        private final Extension extension;
        private final BlockingQueue<Runnable> tasks = new LinkedBlockingDeque<>();
        private final List<Thread> waiting = new CopyOnWriteArrayList<>();
        private final AtomicInteger taskCount = new AtomicInteger();

        public Worker(Extension extension, String name) {
            super(threadGroup, "ExtensionWorkerThread: " + name);
            this.extension = extension;
            setDaemon(false);
        }

        @Override
        public void run() {
            System.out.println("Starting up...");
            while(isAlive() && !isInterrupted()) {
                try {
                    loop();
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("Shutting down...");
            Runnable task;
            while((task = tasks.poll()) != null) {
                task.run();
                taskCount.decrementAndGet();
            }
        }

        protected void loop() throws InterruptedException {
            Runnable task = tasks.take();
            do {
                task.run();
                taskCount.decrementAndGet();
            } while((task = tasks.poll()) != null);
            for(Thread thread : waiting) LockSupport.unpark(thread);
        }

        @Override
        public void execute(@NotNull Runnable runnable) {
            try {
                taskCount.incrementAndGet();
                tasks.put(runnable);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public boolean isQuiescent() {
            return taskCount.get() == 0;
        }

        public void helpShutdown() {
            Runnable task;
            while((task = tasks.poll()) != null) {
                task.run();
                taskCount.decrementAndGet();
            }

            for(Thread thread : waiting) LockSupport.unpark(thread);
        }

        public void waitForExec() {
            try {
                ManagedBlockerImpl blocker = new ManagedBlockerImpl();
                ForkJoinPool.managedBlock(blocker);
                waiting.remove(blocker.thread);
            } catch (InterruptedException ignored) {
            }
        }

        private final class ManagedBlockerImpl implements ForkJoinPool.ManagedBlocker {
            final Thread thread = Thread.currentThread();

            {
                waiting.add(thread);
            }

            @Override
            public boolean block() {
                if(isReleasable())
                    return true;
                LockSupport.park(this);
                return isReleasable();
            }

            @Override
            public boolean isReleasable() {
                return thread.isInterrupted() || isQuiescent();
            }
        }
    }
}
