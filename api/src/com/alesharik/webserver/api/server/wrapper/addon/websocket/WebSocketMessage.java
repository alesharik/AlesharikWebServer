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

package com.alesharik.webserver.api.server.wrapper.addon.websocket;

import com.alesharik.webserver.api.server.wrapper.addon.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.nio.charset.StandardCharsets;

@Immutable
@RequiredArgsConstructor
public class WebSocketMessage implements Message {
    protected final Type type;
    protected final DataType dataType;
    @Getter
    protected final boolean fragment;
    @Getter
    protected final boolean end;
    protected final byte[] data;

    public boolean isPing() {
        return type == Type.PING;
    }

    public boolean isPong() {
        return type == Type.PONG;
    }

    public boolean isString() {
        return dataType == DataType.STRING;
    }

    public boolean isClose() {
        return type == Type.CLOSE;
    }

    @Nonnull
    public String getMessageString() {
        return new String(data, StandardCharsets.UTF_8);
    }

    /**
     * @return internal data array. DO NOT CHANGE IT!
     */
    @Nonnull
    public byte[] getMessage() {
        return data;
    }

    public enum Type {
        PING,
        PONG,
        CLOSE,
        MESSAGE
    }

    public enum DataType {
        STRING,
        BYTE,
        UNKNOWN
    }
}
