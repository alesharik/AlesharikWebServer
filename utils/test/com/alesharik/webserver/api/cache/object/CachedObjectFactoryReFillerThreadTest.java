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

import org.junit.Test;

import java.io.PrintStream;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class CachedObjectFactoryReFillerThreadTest {
    @Test
    public void testRevoke() throws Exception {
        PrintStream mock = mock(PrintStream.class);
        System.setErr(mock);

        assertTrue(CachedObjectFactoryReFillerThread.thread.get().isAlive());

        CachedObjectFactoryReFillerThread.thread.get().interrupt();

        Thread.sleep(100);

        assertTrue(CachedObjectFactoryReFillerThread.thread.get().isAlive());
    }
}