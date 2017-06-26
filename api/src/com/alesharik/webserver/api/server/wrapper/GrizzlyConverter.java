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

package com.alesharik.webserver.api.server.wrapper;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class GrizzlyConverter {
    public static PortRange getPortRange(org.glassfish.grizzly.PortRange portRange) {
        return new PortRange(portRange.getLower(), portRange.getUpper());
    }

    public static org.glassfish.grizzly.PortRange getPortRange(PortRange portRange) {
        return new org.glassfish.grizzly.PortRange(portRange.getLower(), portRange.getUpper());
    }
}
