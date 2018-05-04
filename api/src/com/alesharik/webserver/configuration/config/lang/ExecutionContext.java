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

package com.alesharik.webserver.configuration.config.lang;

import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;

public enum ExecutionContext {
    GLOBAL,
    MODULE,
    CALL;

    @Contract("null -> !null; !null -> _")
    public static ExecutionContext parse(@Nullable String name) {
        if(name == null)
            return CALL;
        else if("global".equalsIgnoreCase(name))
            return GLOBAL;
        else if("module".equalsIgnoreCase(name))
            return MODULE;
        else if("call".equalsIgnoreCase(name))
            return CALL;
        else
            return null;
    }
}
