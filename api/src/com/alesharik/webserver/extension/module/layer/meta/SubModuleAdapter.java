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

package com.alesharik.webserver.extension.module.layer.meta;

import com.alesharik.webserver.extension.module.meta.CustomData;
import com.alesharik.webserver.extension.module.meta.MetaInvokeException;

import javax.annotation.Nonnull;

/**
 * This class warps all fancy annotated methods with basic API
 */
public interface SubModuleAdapter {
    /**
     * Invoke all start methods. This method has call protection(it does nothing if module is already started)
     *
     * @throws MetaInvokeException if one of the start methods throws an exception
     */
    void start() throws MetaInvokeException;

    /**
     * Invoke all shutdown methods. This method has call protection(it does nothing if module is already shot down)
     * @throws MetaInvokeException if one of the shutdown methods throws an exception
     */
    void shutdown() throws MetaInvokeException;

    /**
     * Invoke all shutdownNow methods. This method has call protection(it does nothing if module is already shot down)
     * @throws MetaInvokeException if one of the shutdownNow methods throws an exception
     */
    void shutdownNow() throws MetaInvokeException;

    /**
     * Return submodule's name
     * @return the submodule's name
     */
    @Nonnull
    String getName();

    /**
     * Return custom data object
     * @return custom data object
     */
    @Nonnull
    CustomData getCustomData();

    /**
     * Return <code>true</code> if module is running, overwise <code>false</code>
     * @return <code>true</code> - running, <code>false</code> - not
     */
    boolean isRunning();
}
