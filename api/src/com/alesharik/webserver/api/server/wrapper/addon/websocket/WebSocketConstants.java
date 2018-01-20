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

import lombok.experimental.UtilityClass;

import java.nio.ByteOrder;

@UtilityClass
class WebSocketConstants {
    static final ByteOrder ENDIAN = ByteOrder.BIG_ENDIAN;
    static final byte TEXT = 0x01;
    static final byte BYTE = 0x02;
    static final byte CLOSE = 0x08;
    static final byte PING = 0x09;
    static final byte PONG = 0x0A;
    static final byte CONTINUE = 0x00;

    static final int CUT_FIN = 0b10000000;
    static final byte CUT_SRV1 = 0b01000000;
    static final byte CUT_SRV2 = 0b00100000;
    static final byte CUT_SRV3 = 0b00010000;
    static final byte CUT_OPCODE = 0b00001111;
    static final int CUT_MASK = 0b10000000;
    static final byte CUT_LENGTH = 0b01111111;
}
