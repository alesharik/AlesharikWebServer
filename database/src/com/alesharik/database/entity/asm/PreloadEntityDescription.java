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

package com.alesharik.database.entity.asm;

import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
final class PreloadEntityDescription {
    final String className;
    final List<PreloadEntityColumn> columns;
    final boolean lazy;
    final boolean bridge;

    PreloadEntityDescription(String className, boolean lazy, boolean bridge) {
        this.className = className;
        this.lazy = lazy;
        this.bridge = bridge;
        this.columns = new CopyOnWriteArrayList<>();
    }

    public EntityDescription build(Class<?> clazz) {
        return new EntityDescription(clazz, columns, lazy, bridge);
    }
}
