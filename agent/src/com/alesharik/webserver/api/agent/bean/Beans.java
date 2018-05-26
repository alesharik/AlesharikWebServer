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

package com.alesharik.webserver.api.agent.bean;

import com.alesharik.webserver.base.bean.Bean;
import com.alesharik.webserver.base.bean.Wire;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implementation for base.beans
 * @see Wire
 * @see javax.annotation.PreDestroy
 * @see javax.annotation.PostConstruct
 */
@UtilityClass
public class Beans {
    @Nonnull
    public static <T> T getBean(Class<T> clazz) {
        return getBean(clazz, null);
    }

    @Nonnull
    public static <T> T getBean(Class<T> clazz, @Nullable Bean beanOverride) {
        return Contexts.getDefaultBeanContext().getBean(clazz, beanOverride);
    }
}
