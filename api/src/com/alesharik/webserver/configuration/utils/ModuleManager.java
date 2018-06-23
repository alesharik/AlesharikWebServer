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

import com.alesharik.webserver.configuration.module.meta.ModuleProvider;
import org.apache.commons.collections4.MultiValuedMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface ModuleManager extends ModuleProvider {
    @Nonnull
    List<Module> getModules();

    @Nullable
    Module getModule(@Nonnull String name);

    @Nonnull
    ModuleClassLoader getClassLoader(@Nonnull Module module);

    @Nonnull
    MultiValuedMap<SharedLibrary, Module> getModulesWithUnmetDependencies();

    void addListener(@Nonnull UpdateListener listener);

    void removeListener(@Nonnull UpdateListener listener);

    interface UpdateListener {
        default void onModuleAdd(@Nonnull Module module) {
        }

        default void onModuleUpdate(@Nonnull Module module) {
        }

        default void onModuleReload(@Nonnull Module module) {
        }

        default void onModuleDelete(@Nonnull Module module) {
        }
    }
}
