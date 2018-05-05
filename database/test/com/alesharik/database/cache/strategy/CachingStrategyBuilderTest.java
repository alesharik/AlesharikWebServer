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

package com.alesharik.database.cache.strategy;

import org.junit.After;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

public class CachingStrategyBuilderTest {
    @After
    public void tearDown() {
        System.setProperty("alesharikwebserver-database-cache-timer-provider", "");
    }

    @Test
    public void testDefault() {
        ScheduledExecutorService executorService = CachingStrategyBuilder.get();
        assertFalse(mockingDetails(executorService).isMock());
        assertFalse(executorService.isShutdown());
        executorService.shutdown();
    }

    @Test
    public void testCustom() {
        System.setProperty("alesharikwebserver-database-cache-timer-provider", "com.alesharik.database.cache.strategy.CachingStrategyBuilderTest$TestProvider");
        assertTrue(mockingDetails(CachingStrategyBuilder.get()).isMock());
    }

    static final class TestProvider implements DefaultDatabaseCacheTimerProvider {

        @Nonnull
        @Override
        public ScheduledExecutorService getTimer() {
            return mock(ScheduledExecutorService.class);
        }
    }
}