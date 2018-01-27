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
 * If {@link BackgroundTask} implements this interface, {@link BackgroundExecutor} will get it's state from it
 *
 * @see TaskProgress
 * @see BackgroundExecutor
 * @see BackgroundTask
 */
@ThreadSafe
public interface ProgressProvider {
    /**
     * Return max progress value
     * @return non-negative value
     */
    @Nonnegative
    int getMaxProgress();

    /**
     * Return current progress
     *
     * @return non-negative value. Must be < max progress value
     */
    @Nonnegative
    int getCurrentProgress();

    /**
     * Return current state name. This name will be used to display what the task is currently doing
     *
     * @return the current state name. Empty string disables this feature
     */
    @Nonnull
    default String getCurrentStateName() {
        return "";
    }
}
