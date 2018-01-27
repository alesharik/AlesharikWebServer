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
import java.util.Map;

/**
 * Background executor executes {@link BackgroundTask}s
 *
 * @see BackgroundTask
 * @see TaskProgress
 */
@ThreadSafe
public interface BackgroundExecutor {
    /**
     * Execute the task
     *
     * @param task the task
     */
    void executeTask(@Nonnull BackgroundTask task);

    /**
     * Return task progress for it's unique parameters
     *
     * @param name the task name
     * @param id   the task id
     * @return task progress or <code>null</code> if task not found
     */
    @Nullable
    TaskProgress getProgress(@Nonnull String name, @Nonnegative long id);

    /**
     * Return tasks for it's name
     *
     * @param name the tasks name
     * @return mapped tasks ids and their's progresses
     */
    @Nonnull
    Map<Long, TaskProgress> getProgressForName(@Nonnull String name);

    /**
     * Cancel the task
     *
     * @param name the task name
     * @param id   the task id
     * @return <code>true</code> - success, <code>false</code> - not found/done/cancelled
     */
    boolean cancel(@Nonnull String name, @Nonnull long id);
}
