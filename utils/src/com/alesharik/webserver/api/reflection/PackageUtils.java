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

package com.alesharik.webserver.api.reflection;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@UtilityClass
public final class PackageUtils {

    /**
     * Return package only if requested package contains package-info class, overwise return <code>null</code>
     *
     * @param pkg the package name
     * @return requested package or null
     */
    @Nullable
    public static Package getPackage(@Nonnull String pkg) {
        try {
            Class.forName(pkg + ".package-info", false, PackageUtils.class.getClassLoader());
            return Package.getPackage(pkg);
        } catch (ClassNotFoundException e) {
            return null; //package-info not found
        }
    }
}
