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

package com.alesharik.webserver.module.http.addon.websocket.processor;

import com.alesharik.webserver.api.cache.object.CachedObjectFactory;
import com.alesharik.webserver.api.cache.object.Recyclable;
import com.alesharik.webserver.api.cache.object.SmartCachedObjectFactory;
import com.alesharik.webserver.module.http.addon.Message;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.nio.charset.StandardCharsets;

@Immutable
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebSocketMessage implements Message, Recyclable {
    private static final CachedObjectFactory<WebSocketMessage> FACTORY = new SmartCachedObjectFactory<>(WebSocketMessage::new);
    private Type type;
    private DataType dataType;
    private boolean fragment;
    private boolean end;
    private byte[] data;

    public boolean isPing() {
        return type == Type.PING;
    }

    public boolean isPong() {
        return type == Type.PONG;
    }

    public boolean isClose() {
        return type == Type.CLOSE;
    }

    public boolean isString() {
        return dataType == DataType.STRING && type == Type.MESSAGE;
    }

    public boolean isByte() {
        return dataType == DataType.BYTE && type == Type.MESSAGE;
    }

    public boolean isEnd() {
        return type != Type.MESSAGE || end;
    }

    public boolean isFragment() {
        return fragment & type == Type.MESSAGE;
    }

    public boolean isConnect() {
        return type == Type.CONNECT;
    }

    public boolean isDataMessage() {
        return type == Type.MESSAGE;
    }

    public CloseReason getCloseReason() {
        return CloseReason.parse(Integer.parseInt(getMessageString()));
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

    @Override
    public void recycle() {
        type = null;
        data = null;
        fragment = false;
        end = false;
        data = null;
    }

    public static WebSocketMessage create(Type type, DataType dataType, boolean fragment, boolean end, byte[] data) {
        WebSocketMessage message = FACTORY.getInstance();
        message.type = type;
        message.dataType = dataType;
        message.fragment = fragment;
        message.end = end;
        message.data = data;
        return message;
    }

    public static void recycle(WebSocketMessage message) {
        FACTORY.putInstance(message);
    }

    public enum Type {
        PING,
        PONG,
        CLOSE,
        MESSAGE,
        CONNECT
    }

    public enum DataType {
        STRING,
        BYTE,
        UNKNOWN
    }

    @RequiredArgsConstructor
    public enum CloseReason {
        NORMAL_1000(1000),
        GONE_1001(1001),
        PROTOCOL_ERROR_1002(1002),
        INCORRECT_DATA_1003(1003),
        NOT_CONSISTENT_1007(1007),
        OTHER_1008(1008),
        TOO_BIG_1009(1009),
        EXTENSION_NEGATION_ERROR_1010(1010),
        UNEXPECTED_ERROR_1011(1011),
        NONE(-1);

        @Getter
        private final int code;

        public static CloseReason parse(int code) {
            for(CloseReason closeReason : values()) {
                if(closeReason.code == code)
                    return closeReason;
            }
            throw new IllegalArgumentException("Code " + code + " not found!");
        }

    }
}
