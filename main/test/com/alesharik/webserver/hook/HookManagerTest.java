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

package com.alesharik.webserver.hook;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.alesharik.webserver.test.TestUtils.assertUtilityClass;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class HookManagerTest {
    @Before
    public void setUp() throws Exception {
        HookManager.hooks.clear();
        HookManager.userDefinedHooks.clear();
    }

    @Test
    public void testListenBad() throws Exception {
        assertTrue(HookManager.hooks.isEmpty());
        HookManager.listen(BadGroup.class);
        assertTrue(HookManager.hooks.isEmpty());
    }

    @Test
    public void testListenGood() throws Exception {
        assertTrue(HookManager.hooks.isEmpty());
        HookManager.listen(Ok.class);
        assertTrue(HookManager.hooks.containsKey("test.ok"));
        assertEquals(Ok.class, HookManager.hooks.get("test.ok").getClass());
    }

    @Test
    public void testOverwriteProtection() throws Exception {
        assertTrue(HookManager.hooks.isEmpty());

        for(int i = 0; i < 1000; i++) {
            HookManager.listen(Ok.class);
        }

        assertTrue(HookManager.hooks.containsKey("test.ok"));
        assertEquals(Ok.class, HookManager.hooks.get("test.ok").getClass());
        assertEquals(1, HookManager.hooks.size());
    }

    @Test
    public void testGetters() throws Exception {
        assertNotNull(HookManager.getClassLoader());
        assertUtilityClass(HookManager.class);
    }

    @Test
    public void testUserDefinedHooks() throws Exception {
        assertTrue(HookManager.userDefinedHooks.isEmpty());

        HookManager.add("test", mock(Hook.class));
        assertFalse(HookManager.userDefinedHooks.isEmpty());

        HookManager.cleanUserDefinedHooks();
        assertTrue(HookManager.userDefinedHooks.isEmpty());
    }

    @Test
    public void testGetHookByName() throws Exception {
        HookManager.listen(Ok.class);

        Hook hook = HookManager.getHookForName("test.ok");
        assertEquals(Ok.class, hook.getClass());

        Hook hook1Mock = mock(Hook.class);
        HookManager.add("test", hook1Mock);
        assertEquals(hook1Mock, HookManager.getHookForName("test"));

        assertNull(HookManager.getHookForName("wat"));
    }

    private static final class BadGroup implements Hook {

        @Nonnull
        @Override
        public String getName() {
            return "test";
        }

        @Nullable
        @Override
        public String getGroup() {
            return null;
        }

        @Override
        public void listen(@Nullable Object sender, @Nonnull Object[] args) {

        }
    }

    private static final class Ok implements Hook {

        @Nonnull
        @Override
        public String getName() {
            return "ok";
        }

        @Nullable
        @Override
        public String getGroup() {
            return "test";
        }

        @Override
        public void listen(@Nullable Object sender, @Nonnull Object[] args) {

        }
    }
}