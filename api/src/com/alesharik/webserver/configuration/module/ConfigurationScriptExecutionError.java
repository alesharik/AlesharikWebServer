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

package com.alesharik.webserver.configuration.module;

import com.alesharik.webserver.configuration.config.lang.ExternalLanguageHelper;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationCodeElement;
import lombok.EqualsAndHashCode;

import javax.script.ScriptException;

@EqualsAndHashCode(callSuper = true)
public class ConfigurationScriptExecutionError extends ConfigurationError {
    private static final long serialVersionUID = -3101932142610545397L;

    private final String message;
    private final ScriptException exception;

    public ConfigurationScriptExecutionError(ScriptException e) {
        String msg = e.getLocalizedMessage();
        if(e.getColumnNumber() != -1)
            msg += " at column " + e.getColumnNumber();
        if(e.getLineNumber() != -1)
            msg += " at line " + e.getLineNumber();
        this.message = msg;
        this.exception = e;
    }

    public ConfigurationScriptExecutionError(ExternalLanguageHelper helper, ScriptException e) {
        String msg = e.getLocalizedMessage();
        if(e.getColumnNumber() != -1)
            msg += " at column " + e.getColumnNumber();
        if(e.getLineNumber() != -1)
            msg += " at line " + e.getLineNumber();
        this.message = "Script helper init error! Helper: " + helper + ", message: " + msg;
        this.exception = e;
    }

    public ConfigurationScriptExecutionError(ConfigurationCodeElement element, ScriptException e) {
        String msg = e.getLocalizedMessage();
        if(e.getColumnNumber() != -1)
            msg += " at column " + e.getColumnNumber();
        if(e.getLineNumber() != -1)
            msg += " at line " + e.getLineNumber();
        this.message = "Code element error! Language: " + element.getLanguageName() + ", message: " + msg + ", code: \n" + element.getCode();
        this.exception = e;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getLocalizedMessage() {
        return message;
    }

    @Override
    public ScriptException getCause() {
        return exception;
    }
}
