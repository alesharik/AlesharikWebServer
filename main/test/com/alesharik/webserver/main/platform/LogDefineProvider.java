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

package com.alesharik.webserver.main.platform;

import com.alesharik.webserver.configuration.config.ext.DefineEnvironment;
import com.alesharik.webserver.configuration.config.ext.DefineProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LogDefineProvider implements DefineProvider {
    public static String DEF = "";

    @Nonnull
    @Override
    public String getName() {
        return "LOG_MOCK";
    }

    @Nullable
    @Override
    public String getDefinition(@Nonnull DefineEnvironment environment) {
        return DEF;
    }
}
