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

package com.alesharik.webserver.main.script;

import com.alesharik.webserver.configuration.config.ext.ScriptExecutionError;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import static org.junit.Assert.*;

public class JavaScriptEngineProviderTest {
    private JavaScriptEngineProvider provider;

    @Before
    public void setUp() {
        provider = new JavaScriptEngineProvider();
    }

    @Test
    public void name() {
        assertEquals("javascript", provider.getName());
    }

    @Test
    public void execJS() throws ScriptException {
        Object res = provider.getEngineFactory().getScriptEngine().eval("1;");
        assertEquals(1, res);
    }

    @Test
    public void helperHasFunctionInEngine() throws ScriptException {
        ScriptEngine engine = provider.getEngineFactory().getScriptEngine();
        engine.eval("function a() {}");

        assertTrue(provider.getHelper().hasFunction("a", engine));
        assertFalse(provider.getHelper().hasFunction("b", engine));
    }

    @Test
    public void helperHasFunctionInCode() {
        assertTrue(provider.getHelper().hasFunction("a", "function a() {}"));
        assertFalse(provider.getHelper().hasFunction("b", "function a() {}"));
    }

    @Test(expected = ScriptExecutionError.class)
    public void helperHasErrorFunctionInCode() {
        provider.getHelper().hasFunction("a", "qwert();");
    }

    @Test
    public void helperExecFunction() throws ScriptException {
        ScriptEngine engine = provider.getEngineFactory().getScriptEngine();
        engine.eval("function a() { return \"qwerty\"; }");
        assertEquals("qwerty", provider.getHelper().executeFunction("a", engine));
    }

    @Test
    public void helperExecVoidFunction() throws ScriptException {
        ScriptEngine engine = provider.getEngineFactory().getScriptEngine();
        engine.eval("function a() { }");
        assertNull(provider.getHelper().executeFunction("a", engine));
    }

    @Test(expected = IllegalArgumentException.class)
    public void helperExecNotFoundFunction() throws ScriptException {
        ScriptEngine engine = provider.getEngineFactory().getScriptEngine();
        engine.eval("function a() { }");
        provider.getHelper().executeFunction("q", engine);
        fail();
    }

    @Test(expected = ScriptExecutionError.class)
    public void helperExecErrorFunction() throws ScriptException {
        ScriptEngine engine = provider.getEngineFactory().getScriptEngine();
        engine.eval("function a() { qwe(); }");
        provider.getHelper().executeFunction("a", engine);
        fail();
    }
}