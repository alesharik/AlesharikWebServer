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

package com.alesharik.webserver.api.utils.crypto.crc;

import java.security.Provider;
import java.security.Security;

public final class CRC32Provider extends Provider {
    private static final long serialVersionUID = -1418641541999795546L;

    static {
        Security.addProvider(new CRC32Provider());
    }

    /**
     * Constructs a provider with the specified name, version number,
     * and information.
     */
    public CRC32Provider() {
        super("CRC32", 1.0, "CRC32 provider");
        put("MessageDigest.CRC32", "com.alesharik.webserver.api.utils.crypto.crc.CRC32MessageDigest");
    }
}
