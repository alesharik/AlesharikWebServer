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

package com.alesharik.webserver.configuration.module;

import com.alesharik.webserver.api.agent.bean.AbstractBeanContext;
import com.alesharik.webserver.base.bean.context.BeanContext;
import com.alesharik.webserver.base.bean.context.BeanContextManager;
import com.alesharik.webserver.base.bean.context.Manager;
import com.alesharik.webserver.configuration.config.ext.ScriptManager;
import com.alesharik.webserver.configuration.module.meta.ScriptElementConverter;
import com.alesharik.webserver.configuration.utils.CoreModuleManager;
import com.alesharik.webserver.main.Main;

import javax.annotation.Nonnull;

@Manager(ModuleBeanContext.Manager.class)
public final class ModuleBeanContext extends AbstractBeanContext {
    private ModuleBeanContext() {
        super("module-context");
        linkSingleton(CoreModuleManager.class, Main.getCoreModuleManager());
        linkSingleton(ScriptManager.class, Main.getScriptEngine());
        linkSingleton(ScriptElementConverter.class, Main.getScriptEngine());
    }

    @Override
    public <T> void linkSingleton(Class<T> clazz, T instance) {
        super.linkSingleton(clazz, instance);
    }

    public static final class Manager implements BeanContextManager {

        @Nonnull
        @Override
        public BeanContext createContext() {
            return new ModuleBeanContext();
        }
    }
}
