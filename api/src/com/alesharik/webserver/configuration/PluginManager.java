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

package com.alesharik.webserver.configuration;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;

@ThreadSafe
@Deprecated
public abstract class PluginManager extends Thread implements PluginManagerMXBean {
    protected final File workingFolder;
    protected final boolean hotReloadEnabled;

    public PluginManager(File workingFolder, boolean hotReloadEnabled) {
        this.workingFolder = workingFolder;
        this.hotReloadEnabled = hotReloadEnabled;
    }

    /**
     * Return true if manager finish all jobs
     */
    public abstract boolean isFree();

    public interface Factory {
        PluginManager newInstance(File pluginsFolder, boolean hotReload);
    }
}
