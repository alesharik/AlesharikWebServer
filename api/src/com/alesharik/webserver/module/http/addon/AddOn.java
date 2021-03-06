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

package com.alesharik.webserver.module.http.addon;

import javax.annotation.Nonnull;

/**
 * Addons extend HTTP server's capabilities and provide support for non-HTTP protocols
 *
 * @param <MsgProc>
 */
public interface AddOn<MsgProc extends MessageProcessor> {
    /**
     * Return addon's unique name. It will be used in {@link com.alesharik.webserver.module.http.http.Response#upgrade(String)} to switch protocols
     * @return addon's unique name
     */
    @Nonnull
    String getName();

    /**
     * Return socket handler for specified message processor
     * @param processor the message processor
     * @return new socket handler
     */
    @Nonnull
    AddOnSocketHandler getHandler(MsgProc processor);
}
