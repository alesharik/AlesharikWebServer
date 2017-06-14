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

package com.alesharik.webserver.control;

import com.alesharik.webserver.configuration.Module;
import com.alesharik.webserver.control.dashboard.websocket.DashboardWebSocketPlugin;
import com.alesharik.webserver.control.dashboard.websocket.DashboardWebSocketPluginListener;

import javax.annotation.Nonnull;

public interface ControlServer extends Module {
    <T extends DashboardWebSocketPlugin> void addDashboardWebSocketPluginListener(@Nonnull String name, @Nonnull Class<T> pluginClazz, @Nonnull DashboardWebSocketPluginListener<T> listener);

    <T extends DashboardWebSocketPlugin> void removeDashboardWebSocketPluginListener(@Nonnull String name, @Nonnull DashboardWebSocketPluginListener<T> listener);
}
