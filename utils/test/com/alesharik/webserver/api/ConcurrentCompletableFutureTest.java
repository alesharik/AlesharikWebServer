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

package com.alesharik.webserver.api;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ConcurrentCompletableFutureTest {
    private ConcurrentCompletableFuture<String> future = new ConcurrentCompletableFuture<>();

    @Before
    public void setUp() {
        future = new ConcurrentCompletableFuture<>();
    }

    @Test(timeout = 2000)
    public void testOKCompletion() {
        Thread completer = new Thread(() -> {
            try {
                Thread.sleep(1000);
                future.set("test");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        completer.start();
        assertFalse(future.isDone());
        assertFalse(future.isCancelled());
        String ret = future.get();
        assertEquals("test", ret);
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
    }

    @Test(timeout = 4000)
    public void testOKCompletionWithTimeout() {
        Thread completer = new Thread(() -> {
            try {
                Thread.sleep(2000);
                future.set("test");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        completer.start();
        String ret = future.get(1, TimeUnit.SECONDS);
        assertNull(ret);
        assertFalse(future.isDone());
        assertFalse(future.isCancelled());
        ret = future.get(2, TimeUnit.SECONDS);
        assertEquals("test", ret);
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
    }

    @Test(timeout = 2000)
    public void testCancelCompletion() {
        Thread completer = new Thread(() -> {
            try {
                Thread.sleep(1000);
                future.cancel(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        completer.start();
        String ret = future.get();
        assertNull(ret);
        assertFalse(future.isDone());
        assertTrue(future.isCancelled());
    }

    @Test(timeout = 4000)
    public void testCancelCompletionWithTimeout() {
        Thread completer = new Thread(() -> {
            try {
                Thread.sleep(2000);
                future.cancel(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        completer.start();
        String ret = future.get(1, TimeUnit.SECONDS);
        assertNull(ret);
        assertFalse(future.isDone());
        assertFalse(future.isCancelled());

        ret = future.get(2, TimeUnit.SECONDS);
        assertNull(ret);
        assertTrue(future.isCancelled());
        assertFalse(future.isDone());
    }
}