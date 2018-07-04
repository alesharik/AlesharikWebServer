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

package com.alesharik.webserver.module.http.addon.websocket.impl;

import lombok.experimental.UtilityClass;

import java.nio.ByteOrder;

@UtilityClass
class WebSocketConstants {
    public static final ByteOrder ENDIAN = ByteOrder.BIG_ENDIAN;
    public static final byte TEXT = 0x01;
    public static final byte BYTE = 0x02;
    public static final byte CLOSE = 0x08;
    public static final byte PING = 0x09;
    public static final byte PONG = 0x0A;
    public static final byte CONTINUE = 0x00;

    public static final int CUT_FIN = 0b10000000;
    public static final byte CUT_SRV1 = 0b01000000;
    public static final byte CUT_SRV2 = 0b00100000;
    public static final byte CUT_SRV3 = 0b00010000;
    public static final byte CUT_OPCODE = 0b00001111;
    public static final int CUT_MASK = 0b10000000;
    public static final byte CUT_LENGTH = 0b01111111;
}
