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

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Background task is simple runnable, which will be executed in {@link BackgroundExecutor}
 *
 * @see BackgroundExecutor
 * @see ProgressProvider
 */
@ThreadSafe
public interface BackgroundTask extends Runnable, Comparable<BackgroundTask> {
    /**
     * Return task unique id. The id must be unique for task name, not for all tasks
     *
     * @return non-negative id
     */
    @Nonnegative
    long getId();

    /**
     * Return task name. Task names allow {@link BackgroundExecutor} to group tasks
     *
     * @return the task name
     */
    @Nonnull
    String getName();

    /**
     * Return task type. {@link BackgroundExecutor} can change priority depending of the task type
     *
     * @return the task type
     */
    @Nonnull
    BackgroundTaskType getType();

    /**
     * {@link BackgroundExecutor} can use this comparator to sort tasks in execution queue
     */
    @Override
    default int compareTo(@NotNull BackgroundTask o) {
        return Long.compare(getId(), o.getId());
    }
}
