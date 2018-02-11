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

package com.alesharik.webserver.configuration.config.ext;

import com.alesharik.webserver.api.agent.ClassHoldingContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefineManagerTest {

    @Before
    public void setUp() throws Exception {
        DefineManager.providers.create();
        DefineManager.listen(DefineTest.class);
    }

    @After
    public void tearDown() throws Exception {
        DefineManager.providers.destroy();
    }

    @Test
    public void getDefinition() {
        DefineEnvironment env = mock(DefineEnvironment.class);
        when(env.getDefinition("asd")).thenReturn("asd");

        assertEquals("testasd", DefineManager.getDefinition("test", env));
    }

    @Test
    public void getNotExistingDefinition() {
        DefineEnvironment env = mock(DefineEnvironment.class);
        when(env.getDefinition("asd")).thenReturn("asd");

        assertNull(DefineManager.getDefinition("asd", env));
    }

    @Test
    public void tryRegisterSameDefineProvider() {
        for(int i = 0; i < 100; i++) {
            DefineManager.listen(DefineTest.class);
        }

        getDefinition();
    }

    @Test
    public void doNotCrashWhenListenIncorrectClass() {
        DefineManager.listen(DefineTestIncorrect.class);
        assertNull(DefineManager.getDefinition("asd", mock(DefineEnvironment.class)));
    }

    @Test
    public void checkClassReloading() {
        ClassHoldingContext context = DefineManager.providers;
        context.pause();
        DefineManager.listen(DefineTest.class);
        context.reload(DefineTest.class);
        DefineManager.listen(DefineTest.class);
        context.resume();
        getDefinition();
    }

    private static final class DefineTest implements DefineProvider {

        @Nonnull
        @Override
        public String getName() {
            return "test";
        }

        @Nullable
        @Override
        public String getDefinition(@Nonnull DefineEnvironment environment) {
            return "test" + environment.getDefinition("asd");
        }
    }

    private static final class DefineTestIncorrect implements DefineProvider {

        public DefineTestIncorrect() {
            throw new RuntimeException();
        }

        @Nonnull
        @Override
        public String getName() {
            return "asd";
        }

        @Nullable
        @Override
        public String getDefinition(@Nonnull DefineEnvironment environment) {
            return "asd" + environment.getDefinition("asd");
        }
    }
}