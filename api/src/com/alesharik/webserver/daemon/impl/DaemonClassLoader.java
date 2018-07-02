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

package com.alesharik.webserver.daemon.impl;

import com.alesharik.webserver.api.agent.CloseableClassLoader;
import com.alesharik.webserver.daemon.Daemon;
import com.alesharik.webserver.daemon.DaemonApi;
import com.alesharik.webserver.daemon.DaemonApiWrapper;
import lombok.Getter;
import lombok.Setter;

@Deprecated
final class DaemonClassLoader extends ClassLoader implements CloseableClassLoader {
    private final DaemonApiWrapperImpl daemonApiWrapper;

    @Getter
    private volatile Daemon daemon;

    private volatile boolean isClosed = false;

    public DaemonClassLoader(ClassLoader parent) {
        super(parent);
        daemonApiWrapper = new DaemonApiWrapperImpl();
    }

    @Override
    public void close() {
        isClosed = true;
        daemon.shutdown();
        daemon = null;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    void setDaemon(Daemon daemon) {
        this.daemon = daemon;
        this.daemonApiWrapper.daemonApi = daemon.getApi();
    }

    DaemonApiWrapper getApi() {
        return daemonApiWrapper;
    }

    public static DaemonClassLoader getClassLoader(Daemon daemon) {
        ClassLoader cl = daemon.getClass().getClassLoader();
        if(!(cl instanceof DaemonClassLoader))
            throw new IllegalArgumentException("All daemons must use DaemonClassLoader");
        return ((DaemonClassLoader) cl);
    }

    private static final class DaemonApiWrapperImpl extends DaemonApiWrapper {
        @Setter
        private volatile DaemonApi daemonApi;

        @Override
        public DaemonApi get() {
            return daemonApi;
        }

        @Override
        public boolean available() {
            return daemonApi != null;
        }
    }
}
