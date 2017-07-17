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

package com.alesharik.webserver.api.server.wrapper.server;

import com.alesharik.webserver.configuration.SubModule;
import org.w3c.dom.Element;

import javax.annotation.Nullable;

/**
 * ServerSocketWrapper used by HTTP server for server socket management. Realisations are specified by user in config. All
 * realisation classes must be named with @{@link com.alesharik.webserver.api.name.Named}.
 * Realisation classes also must have empty public constructor!
 */
public interface ServerSocketWrapper extends SocketProvider, SubModule {
    /**
     * Parse module configuration
     *
     * @param element the configuration element
     */
    void parseConfig(@Nullable Element element);
}
