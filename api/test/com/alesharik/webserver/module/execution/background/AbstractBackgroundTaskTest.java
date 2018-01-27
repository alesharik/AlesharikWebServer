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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.junit.Assert.assertEquals;

public class AbstractBackgroundTaskTest {
    @Before
    public void setUp() throws Exception {
        AbstractBackgroundTask.counter.set(0);
    }

    @After
    public void tearDown() throws Exception {
        AbstractBackgroundTask.counter.set(0);
    }

    @Test
    public void getId() {
        for(int i = 0; i < 100; i++) {
            AbstractBackgroundTask task = new AbstractBackgroundTask() {
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
            };
            assertEquals(i, task.getId());
            assertEquals(i, task.getId());
        }
    }

    @Test
    public void getIdOverflow() {
        AbstractBackgroundTask.counter.set(Long.MAX_VALUE);
        assertEquals(0, new AbstractBackgroundTask() {
            @Override
            public void run() {

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
        }.getId());
    }
}