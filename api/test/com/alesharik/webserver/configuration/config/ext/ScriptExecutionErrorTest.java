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

import com.alesharik.webserver.configuration.config.lang.ExternalLanguageHelper;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationCodeElement;
import org.junit.Test;

import javax.script.ScriptException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScriptExecutionErrorTest {
    @Test
    public void scriptExceptionCtorWithoutAnything() {
        ScriptException exception = new ScriptException("a");
        ScriptExecutionError error = new ScriptExecutionError(exception);

        assertSame(exception, error.getCause());
        assertEquals("a", error.getLocalizedMessage());
        assertEquals("a", error.getMessage());
    }

    @Test
    public void scriptExceptionCtorWithLine() {
        ScriptException exception = new ScriptException("a", "", 12);
        ScriptExecutionError error = new ScriptExecutionError(exception);

        assertSame(exception, error.getCause());
        assertEquals("a at line 12", error.getLocalizedMessage());
        assertEquals("a at line 12", error.getMessage());
    }

    @Test
    public void scriptExceptionCtorWithLineAndColumn() {
        ScriptException exception = new ScriptException("a", "", 12, 123);
        ScriptExecutionError error = new ScriptExecutionError(exception);

        assertSame(exception, error.getCause());
        assertEquals("a at line 12 at column 123", error.getLocalizedMessage());
        assertEquals("a at line 12 at column 123", error.getMessage());
    }

    @Test
    public void scriptExceptionAndHelperCtorWithoutAnything() {
        ExternalLanguageHelper helper = mock(ExternalLanguageHelper.class);
        when(helper.toString()).thenReturn("asd");
        ScriptException exception = new ScriptException("a");
        ScriptExecutionError error = new ScriptExecutionError(helper, exception);

        assertSame(exception, error.getCause());
        assertEquals("ExternalLanguageHelper init error! Helper: asd, message: a", error.getLocalizedMessage());
        assertEquals("ExternalLanguageHelper init error! Helper: asd, message: a", error.getMessage());
    }

    @Test
    public void scriptExceptionAndHelperCtorWithLine() {
        ExternalLanguageHelper helper = mock(ExternalLanguageHelper.class);
        when(helper.toString()).thenReturn("asd");
        ScriptException exception = new ScriptException("a", "", 12);
        ScriptExecutionError error = new ScriptExecutionError(helper, exception);

        assertSame(exception, error.getCause());
        assertEquals("ExternalLanguageHelper init error! Helper: asd, message: a at line 12", error.getLocalizedMessage());
        assertEquals("ExternalLanguageHelper init error! Helper: asd, message: a at line 12", error.getMessage());
    }

    @Test
    public void scriptExceptionAndHelperCtorWithLineAndColumn() {
        ExternalLanguageHelper helper = mock(ExternalLanguageHelper.class);
        when(helper.toString()).thenReturn("asd");
        ScriptException exception = new ScriptException("a", "", 12, 123);
        ScriptExecutionError error = new ScriptExecutionError(helper, exception);

        assertSame(exception, error.getCause());
        assertEquals("ExternalLanguageHelper init error! Helper: asd, message: a at line 12 at column 123", error.getLocalizedMessage());
        assertEquals("ExternalLanguageHelper init error! Helper: asd, message: a at line 12 at column 123", error.getMessage());
    }

    @Test
    public void scriptExceptionAndCodeElementCtorWithoutAnything() {
        ConfigurationCodeElement element = mock(ConfigurationCodeElement.class);
        when(element.getCode()).thenReturn("asdas");
        when(element.getLanguageName()).thenReturn("wqerty");
        ScriptException exception = new ScriptException("a");
        ScriptExecutionError error = new ScriptExecutionError(element, exception);

        assertSame(exception, error.getCause());
        assertEquals("Code element error! Language: wqerty, message: a, code: \nasdas", error.getLocalizedMessage());
        assertEquals("Code element error! Language: wqerty, message: a, code: \nasdas", error.getMessage());
    }

    @Test
    public void scriptExceptionAndCodeElementCtorWithLine() {
        ConfigurationCodeElement element = mock(ConfigurationCodeElement.class);
        when(element.getCode()).thenReturn("asdas");
        when(element.getLanguageName()).thenReturn("wqerty");
        ScriptException exception = new ScriptException("a", "", 12);
        ScriptExecutionError error = new ScriptExecutionError(element, exception);

        assertSame(exception, error.getCause());
        assertEquals("Code element error! Language: wqerty, message: a at line 12, code: \nasdas", error.getLocalizedMessage());
        assertEquals("Code element error! Language: wqerty, message: a at line 12, code: \nasdas", error.getMessage());
    }

    @Test
    public void scriptExceptionAndCodeElementCtorWithLineAndColumn() {
        ConfigurationCodeElement element = mock(ConfigurationCodeElement.class);
        when(element.getCode()).thenReturn("asdas");
        when(element.getLanguageName()).thenReturn("wqerty");
        ScriptException exception = new ScriptException("a", "", 12, 123);
        ScriptExecutionError error = new ScriptExecutionError(element, exception);

        assertSame(exception, error.getCause());
        assertEquals("Code element error! Language: wqerty, message: a at line 12 at column 123, code: \nasdas", error.getLocalizedMessage());
        assertEquals("Code element error! Language: wqerty, message: a at line 12 at column 123, code: \nasdas", error.getMessage());
    }
}