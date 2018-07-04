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

package com.alesharik.webserver.module.http.http.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * If port is -1, then this {@link Host} instance don't have any port
 */
@EqualsAndHashCode
@ToString
@Getter
public class Host {
    protected final String host;
    protected final int port;

    public Host(String host) {
        this(host, -1);
    }

    public Host(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean hasPort() {
        return port != -1;
    }
}
