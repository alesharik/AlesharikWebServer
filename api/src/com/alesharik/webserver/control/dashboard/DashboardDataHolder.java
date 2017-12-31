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

package com.alesharik.webserver.control.dashboard;

import com.alesharik.webserver.configuration.Layer;
import com.alesharik.webserver.configuration.Module;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Deprecated
public final class DashboardDataHolder implements Module {

    public DashboardDataHolder() {
    }

    @Override
    public void parse(@Nullable Element configNode) {
        //TODO write
    }

    @Override
    public void reload(@Nullable Element configNode) {
        parse(configNode);
    }

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void shutdownNow() {
    }

    @Nonnull
    @Override
    public String getName() {
        return "dashboard-data-holder";
    }

    @Nullable
    @Override
    public Layer getMainLayer() {
        return null;
    }
}
