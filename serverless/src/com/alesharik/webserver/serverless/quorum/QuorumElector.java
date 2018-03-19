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

package com.alesharik.webserver.serverless.quorum;

import com.alesharik.webserver.serverless.RemoteAgent;
import com.alesharik.webserver.serverless.ServerlessAgent;
import lombok.experimental.UtilityClass;

/**
 * This utility class elects agents.
 * Election process:<br>
 * 1. Send election message to chosen agents<br>
 * 2. Generate and save master key
 */
@UtilityClass
public class QuorumElector {
    public static void elect(ServerlessAgent me, boolean includeMe, RemoteAgent... agents) {

    }
}
