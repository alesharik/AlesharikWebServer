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

package com.alesharik.webserver.module.execution.background;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.List;

/**
 * @see BackgroundExecutor
 * @see BackgroundExecutorModule
 */
@ThreadSafe
public interface BackgroundExecutorModuleMXBean {
    /**
     * Return executor thread count. Return <code>-1</code> if this executor doesn't use threads
     *
     * @return the thread count
     */
    int getThreadCount();

    /**
     * Return thread group if exists
     *
     * @return the thread group or <code>null</code>
     */
    @Nullable
    ThreadGroup getThreadGroup();

    /**
     * Return active task count
     *
     * @return the active task count
     */
    @Nonnegative
    long getActiveTaskCount();

    /**
     * Return queued task count. Return <code>-1</code> if executor doesn't use task queues
     *
     * @return the queued task count
     */
    long getQueuedTasksCount();

    /**
     * Return all known task names
     *
     * @return the known task names
     */
    @Nonnull
    List<String> getNames();

    /**
     * Return all task progresses
     *
     * @return the task progresses
     */
    @Nonnull
    List<TaskProgress> getProgress();
}
