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
import javax.annotation.concurrent.ThreadSafe;

/**
 * This interface can be used to monitor task execution progress
 *
 * @see ProgressProvider
 * @see BackgroundExecutor
 */
@ThreadSafe
public interface TaskProgress {
    /**
     * Return <code>true</code> if task did his work
     *
     * @return <code>true</code> if task already did hos work
     */
    boolean isDone();

    /**
     * Return <code>true</code> if task is cancelled
     *
     * @return <code>true</code> if task is cancelled
     */
    boolean isCanceled();

    /**
     * Return <code>true</code> if task is execution at the moment
     *
     * @return <code>true</code>  if task is execution at the moment. <code>false</code> - task is done, cancelled or in a queue
     */
    boolean isStarted();

    /**
     * Return max task progress value. If task doesn't support this feature, return <code>1</code>
     *
     * @return max task progress value
     */
    @Nonnegative
    int getMaxProgress();

    /**
     * Return current task progress value. If task doesn't support this feature, return <code>0</code> if task is in execution, <code>1</code> - task is done/cancelled
     *
     * @return current progress value
     */
    @Nonnegative
    int getCurrentProgress();

    /**
     * Return task name
     *
     * @return the task name
     */
    @Nonnull
    String getTaskName();

    /**
     * Return task id
     *
     * @return the task id
     */
    @Nonnegative
    long getTaskId();

    /**
     * Return current execution state name or empty line
     *
     * @return current execution state name or empty line
     * @see ProgressProvider#getCurrentStateName()
     */
    @Nonnull
    String getExecutionStateName();

    /**
     * Return <code>true</code> if task has execution state name, otherwise <code>false</code>
     *
     * @return <code>true</code> if task has execution state name, otherwise <code>false</code>
     */
    default boolean hasExecutionStateName() {
        return !getExecutionStateName().isEmpty();
    }
}
