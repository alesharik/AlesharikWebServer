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

import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObject;

import javax.annotation.Nullable;
import java.util.List;

public interface CustomEndpointSection {
    List<UseDirective> getUseDirectives();

    interface UseDirective {
        String getName();

        ConfigurationObject getConfiguration();

        List<CustomProperty> getCustomProperties();
    }

    interface CustomProperty {
        String getName();

        List<UseCommand> getUseCommands();
    }

    /**
     * Example: <code>
     * In string 'use a to c' the referent is 'a', the arg is 'to c'
     * </code>
     */
    interface UseCommand {
        String getReferent();

        @Nullable
        String getArg();
    }
}
