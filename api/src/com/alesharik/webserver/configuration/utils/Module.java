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

package com.alesharik.webserver.configuration.utils;

import com.alesharik.webserver.base.bean.context.BeanContext;
import com.alesharik.webserver.configuration.module.meta.CustomData;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This class represents read-only module
 */
public interface Module {
    /**
     * Return module name
     *
     * @return the module name
     */
    @Nonnull
    String getName();

    /**
     * Return <code>true</code> if module is running, otherwise <code>false</code>
     *
     * @return <code>true</code> if module is running, otherwise <code>false</code>
     */
    boolean isRunning();

    /**
     * Return module's data
     *
     * @return module's data
     */
    @Nonnull
    CustomData getData();

    /**
     * Return all dependencies set by {@link com.alesharik.webserver.configuration.module.Dependency}
     *
     * @return all set dependencies
     */
    @Nonnull
    List<SharedLibrary> getDependencies();

    /**
     * Return all runtime-determined dependencies
     *
     * @return all runtime-determined dependencies
     */
    @Nonnull
    List<SharedLibrary> getRuntimeDependencies();

    /**
     * Return module context
     *
     * @return module context
     */
    @Nonnull
    BeanContext getContext();
}
