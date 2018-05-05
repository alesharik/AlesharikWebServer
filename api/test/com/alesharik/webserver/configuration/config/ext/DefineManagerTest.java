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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static com.alesharik.webserver.api.TestUtils.assertUtilityClass;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefineManagerTest {

    @Before
    public void setUp() throws Exception {
        DefineManager.providers.clear();
        DefineManager.listen(DefineTest.class);
        DefineManager.listen(A.class);
        DefineManager.listen(B.class);
        DefineManager.listen(C.class);
        DefineManager.listen(Q.class);
    }

    @After
    public void tearDown() throws Exception {
        DefineManager.providers.clear();
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
    public void testUtility() {
        assertUtilityClass(DefineManager.class);
    }

    @Test
    public void testUnloadClassLoader() throws ClassNotFoundException {
        assertEquals("a", DefineManager.getDefinition("a", mock(DefineEnvironment.class)));
        assertEquals("q", DefineManager.getDefinition("q", mock(DefineEnvironment.class)));

        DefineManager.clearClassLoader(this.getClass().getClassLoader());

        assertNull(DefineManager.getDefinition("a", mock(DefineEnvironment.class)));
        assertNull(DefineManager.getDefinition("q", mock(DefineEnvironment.class)));
    }

    @Test
    public void testIsDefined() {
        assertTrue(DefineManager.isDefined("a", mock(DefineEnvironment.class)));
        assertTrue(DefineManager.isDefined("b", mock(DefineEnvironment.class)));

        assertFalse(DefineManager.isDefined("c", mock(DefineEnvironment.class)));
        assertFalse(DefineManager.isDefined("d", mock(DefineEnvironment.class)));
    }

    @Test
    public void getAllDefines() {
        Map<String, String> defines = DefineManager.getAllDefines(mock(DefineEnvironment.class));

        assertEquals("a", defines.get("a"));
        assertEquals("b", defines.get("b"));
        assertNull(defines.get("c"));
        assertNull(defines.get("d"));
    }

    private static final class A implements DefineProvider {

        @Nonnull
        @Override
        public String getName() {
            return "a";
        }

        @Nullable
        @Override
        public String getDefinition(@Nonnull DefineEnvironment environment) {
            return "a";
        }
    }

    private static final class B implements DefineProvider {

        @Nonnull
        @Override
        public String getName() {
            return "b";
        }

        @Nullable
        @Override
        public String getDefinition(@Nonnull DefineEnvironment environment) {
            return "b";
        }
    }

    private static final class C implements DefineProvider {

        @Nonnull
        @Override
        public String getName() {
            return "c";
        }

        @Nullable
        @Override
        public String getDefinition(@Nonnull DefineEnvironment environment) {
            return null;
        }
    }

    private static final class Q implements DefineProvider {

        @Nonnull
        @Override
        public String getName() {
            return "q";
        }

        @Nullable
        @Override
        public String getDefinition(@Nonnull DefineEnvironment environment) {
            return "q";
        }
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