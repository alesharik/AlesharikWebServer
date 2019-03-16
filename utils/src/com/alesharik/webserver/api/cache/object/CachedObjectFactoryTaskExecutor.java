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

package com.alesharik.webserver.api.cache.object;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This executor is used for scheduling smart cached object factories tasks. 'scheduling' means executing tasks at
 * fixed rate
 */
public interface CachedObjectFactoryTaskExecutor {
    CachedObjectFactoryTaskExecutor DEFAULT = new CachedObjectFactoryTaskExecutor() {
        private final Timer timer = new Timer("CachedObjectFactoryTaskDefaultExecutor", true);
        private final Map<Task, TimerTask> taskMap = new ConcurrentHashMap<>();

        @Override
        public void submit(Task task) {
            TimerTaskImpl timerTask = new TimerTaskImpl(task);
            timer.scheduleAtFixedRate(timerTask, 0, task.getInterval());
            taskMap.put(task, timerTask);
        }

        @Override
        public void remove(Task task) {
            if(taskMap.containsKey(task))
                taskMap.remove(task).cancel();
        }

        final class TimerTaskImpl extends TimerTask {
            private final Task task;

            public TimerTaskImpl(Task task) {
                this.task = task;
            }

            @Override
            public void run() {
                task.execute();
            }
        }
    };

    /**
     * Submit task to scheduling
     * @param task the task
     */
    void submit(@Nonnull Task task);

    /**
     * Remove task from scheduling
     * @param task the task
     */
    void remove(@Nonnull Task task);

    /**
     * This interface represents the task
     */
    interface Task {
        /**
         * Task scheduling interval in milliseconds
         * @return task scheduling interval in milliseconds
         */
        @Nonnegative
        long getInterval();

        /**
         * Task main function
         */
        void execute();
    }
}
