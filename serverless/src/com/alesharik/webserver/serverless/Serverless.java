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

package com.alesharik.webserver.serverless;

import com.alesharik.webserver.serverless.discovery.Discovery;
import com.alesharik.webserver.serverless.heartbeat.HeartbeatService;
import com.alesharik.webserver.serverless.message.MessageManager;
import com.alesharik.webserver.serverless.transport.Transport;
import com.alesharik.webserver.serverless.utils.Utility;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.InetAddress;
import java.util.UUID;

/**
 * This class contains factory methods for {@link ServerlessAgent}
 */
@UtilityClass
public class Serverless {
    public static ServerlessAgent newAgent(@Nonnull UUID id, @Nonnull Discovery discovery, @Nonnull Transport transport, @Nonnull HeartbeatService heartbeatService, @Nonnull MessageManager messageManager, @Nonnull ExceptionHandler exceptionHandler, @Nonnull File dataFile, @Nonnull Utility... utilities) {
        return null;//FIXME
    }

    public static ServerlessAgent newAgent(@Nonnull UUID id, @Nonnull Discovery discovery, @Nonnull Transport transport, @Nonnull File dataFile, @Nonnull Utility... utilities) {
        return null;//FIXME
    }

    public static ServerlessAgent newAgent(@Nonnull UUID id, @Nonnull InetAddress group, short discoveryPort, short transportPort, @Nonnull File dataFile, @Nonnull Utility... utilities) {
        return null;//FIXME
    }
}
