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

package com.alesharik.webserver.api;

import com.alesharik.webserver.api.mx.bean.MXBeanManager;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class is used to debounce execution of tasks
 */
@UtilityClass
public class DebounceManager {
    private static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName("DebounceManagerThread");
        return thread;
    });
    private static final Map<Object, Future<?>> tasks = new ConcurrentHashMap<>();

    static {
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        MXBeanManager.registerMXBean(new MXBeanImpl(), MXBean.class,"com.alesharik.webserver.api:type=DebounceManager");
    }

    /**
     * Debounce task
     * @param key task unique key
     * @param runnable the task
     * @param delay debounce delay
     * @param unit debounce delay unit
     */
    public static void debounce(@Nonnull Object key, @Nonnull Runnable runnable, long delay, @Nonnull TimeUnit unit) {
        Future<?> prev = tasks.put(key, service.schedule(() -> {
            tasks.remove(key);
            runnable.run();
        }, delay, unit));
        if(prev != null)
            prev.cancel(true);
    }

    /**
     * Remove task from scheduling
     * @param key task unique key
     * @return <code>true</code> - task is removed, otherwise <code>false</code>
     */
    public static boolean remove(@Nonnull Object key) {
        Future<?> remove = tasks.remove(key);
        if(remove == null)
            return false;
        remove.cancel(true);
        return true;
    }

    private static final class ShutdownHook extends Thread {
        public ShutdownHook() {
            super("DebounceManagerShutdownHook");
        }

        @Override
        public void run() {
            for(Runnable runnable : service.shutdownNow()) {
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * An MXBean for {@link DebounceManager}
     */
    public interface MXBean {
        /**
         * Check if scheduler service is running
         * @return <code>true</code> - service is running, <code>false</code> - service is shut down or terminated
         */
        boolean isRunning();

        /**
         * Return total task count
         * @return total task count
         */
        int getTaskCount();
    }

    private static final class MXBeanImpl implements MXBean {
        public boolean isRunning() {
            return !service.isShutdown() && !service.isTerminated();
        }

        public int getTaskCount() {
            return tasks.size();
        }
    }
}
