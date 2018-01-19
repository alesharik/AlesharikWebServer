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

import com.alesharik.webserver.api.ByteOrderUtils;
import lombok.experimental.UtilityClass;

import java.nio.ByteOrder;

@UtilityClass
class WebSocketConstants {
    static final ByteOrder ENDIAN = ByteOrder.BIG_ENDIAN;
    static final byte TEXT = (byte) ByteOrderUtils.format(0x01, ENDIAN);
    static final byte BYTE = (byte) ByteOrderUtils.format(0x02, ENDIAN);
    static final byte CLOSE = (byte) ByteOrderUtils.format(0x08, ENDIAN);
    static final byte PING = (byte) ByteOrderUtils.format(0x09, ENDIAN);
    static final byte PONG = (byte) ByteOrderUtils.format(0x0A, ENDIAN);
    static final byte CONTINUE = (byte) ByteOrderUtils.format(0x00, ENDIAN);

    static final byte CUT_FIN = (byte) ByteOrderUtils.format(0b10000000, ENDIAN);
    static final byte CUT_SRV1 = (byte) ByteOrderUtils.format(0b01000000, ENDIAN);
    static final byte CUT_SRV2 = (byte) ByteOrderUtils.format(0b00100000, ENDIAN);
    static final byte CUT_SRV3 = (byte) ByteOrderUtils.format(0b00010000, ENDIAN);
    static final byte CUT_OPCODE = (byte) ByteOrderUtils.format(0b00001111, ENDIAN);
    static final byte CUT_MASK = (byte) ByteOrderUtils.format(0b10000000, ENDIAN);
    static final byte CUT_LENGTH = (byte) ByteOrderUtils.format(0b01111111, ENDIAN);
}
