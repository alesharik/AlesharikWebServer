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

package com.alesharik.webserver.extension.module.extension.message;

import com.alesharik.webserver.extension.module.meta.ModuleAdapter;
import com.alesharik.webserver.extension.module.util.Module;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class ModuleStateChangedMessage extends ModuleExtensionMessage {
    private final Module module;
    private final ModuleAdapter adapter;
    private final Object instance;
    private final State state;

    @Override
    public String getName() {
        return "stateChanged";
    }

    public enum State {
        START,
        SHUTDOWN,
        SHUTDOWN_NOW,
        RELOAD
    }
}