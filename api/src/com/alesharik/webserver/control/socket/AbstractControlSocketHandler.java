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

package com.alesharik.webserver.control.socket;

public abstract class AbstractControlSocketHandler {
    protected final ControlSocketSender controlSocketSender;

    public AbstractControlSocketHandler(ControlSocketSender controlSocketSender) {
        this.controlSocketSender = controlSocketSender;
    }

    public abstract void onMessage(String message);

    public abstract void onMessage(Object message);

    public abstract void onOpen();

    public abstract void onClose();
}
