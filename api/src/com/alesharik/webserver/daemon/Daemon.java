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

package com.alesharik.webserver.daemon;

import com.alesharik.webserver.daemon.hook.DaemonHookManager;
import com.alesharik.webserver.logger.NamedLogger;
import org.w3c.dom.Element;

public interface Daemon<A extends DaemonApi> {
    String getName();

    void parseConfig(Element c);

    void setup();

    void run();

    void reload(Element element);

    void shutdown();

    A getApi();

    DaemonHookManager getHookManager();

    DaemonManagementBean getManagementBean();

    NamedLogger getNamedLogger();

    int getPriority();
}
