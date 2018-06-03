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

package com.alesharik.webserver.configuration.module.meta;

import com.alesharik.webserver.base.bean.context.BeanContext;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationTypedObject;

import javax.annotation.Nonnull;

/**
 * This class will be instantiated as a singleton in default context
 */
public interface ConfigurationLinker {
    /**
     * @param object
     * @param module
     * @param provider
     * @param context
     * @throws com.alesharik.webserver.configuration.module.ConfigurationError if config error happens
     */
    void link(@Nonnull ConfigurationTypedObject object, @Nonnull Object module, @Nonnull ModuleProvider provider, @Nonnull BeanContext context);
}
