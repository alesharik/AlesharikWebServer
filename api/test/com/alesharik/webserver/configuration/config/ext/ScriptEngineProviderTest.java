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

import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class ScriptEngineProviderTest {
    @Test
    public void helperExecuteCodeSuccess() throws ScriptException {
        ScriptEngine engine = mock(ScriptEngine.class);
        when(engine.eval("asd")).thenReturn("qwe");

        ScriptEngineProvider.Helper helper = new ScriptEngineProvider.Helper() {
            @Override
            public boolean hasFunction(@Nonnull String name, @Nonnull ScriptEngine engine) {
                return false;
            }

            @Override
            public boolean hasFunction(@Nonnull String name, @Nonnull String code) {
                return false;
            }

            @Nullable
            @Override
            public Object executeFunction(@Nonnull String name, @Nonnull ScriptEngine engine) {
                return null;
            }
        };

        assertEquals("qwe", helper.executeCode("asd", engine));
        verify(engine, times(1)).eval("asd");
    }

    @Test(expected = ScriptExecutionError.class)
    public void helperExecuteCodeError() throws ScriptException {
        ScriptEngine engine = mock(ScriptEngine.class);
        when(engine.eval("asd")).thenThrow(new ScriptException(""));

        ScriptEngineProvider.Helper helper = new ScriptEngineProvider.Helper() {
            @Override
            public boolean hasFunction(@Nonnull String name, @Nonnull ScriptEngine engine) {
                return false;
            }

            @Override
            public boolean hasFunction(@Nonnull String name, @Nonnull String code) {
                return false;
            }

            @Nullable
            @Override
            public Object executeFunction(@Nonnull String name, @Nonnull ScriptEngine engine) {
                return null;
            }
        };
        helper.executeCode("asd", engine);
        fail();
    }
}