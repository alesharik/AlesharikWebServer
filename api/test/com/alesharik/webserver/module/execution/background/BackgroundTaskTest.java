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

import org.junit.Test;

import javax.annotation.Nonnull;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BackgroundTaskTest {

    @Test
    public void compareTo() {
        BackgroundTask task = mock(BackgroundTask.class);
        doCallRealMethod().when(task).compareTo(any());
        doReturn(1L).when(task).getId();
        assertEquals(0, task.compareTo(new BackgroundTask() {
            @Override
            public long getId() {
                return 1;
            }

            @Nonnull
            @Override
            public String getName() {
                return null;
            }

            @Nonnull
            @Override
            public BackgroundTaskType getType() {
                return null;
            }

            @Override
            public void run() {

            }
        }));
        assertEquals(1, task.compareTo(new BackgroundTask() {
            @Override
            public long getId() {
                return 0;
            }

            @Nonnull
            @Override
            public String getName() {
                return null;
            }

            @Nonnull
            @Override
            public BackgroundTaskType getType() {
                return null;
            }

            @Override
            public void run() {

            }
        }));
        assertEquals(-1, task.compareTo(new BackgroundTask() {
            @Override
            public long getId() {
                return 2;
            }

            @Nonnull
            @Override
            public String getName() {
                return null;
            }

            @Nonnull
            @Override
            public BackgroundTaskType getType() {
                return null;
            }

            @Override
            public void run() {

            }
        }));
    }
}