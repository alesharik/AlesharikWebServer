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

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class WebSocketMessageTest {
    @Test
    public void pingMessage() {
        WebSocketMessage message = new WebSocketMessage(WebSocketMessage.Type.PING, WebSocketMessage.DataType.STRING, true, false, null);

        assertTrue(message.isPing());
        assertFalse(message.isPong());
        assertFalse(message.isClose());
        assertTrue(message.isEnd());
        assertFalse(message.isFragment());
        assertFalse(message.isString());
        assertFalse(message.isByte());
    }

    @Test
    public void pongMessage() {
        WebSocketMessage message = new WebSocketMessage(WebSocketMessage.Type.PONG, WebSocketMessage.DataType.STRING, true, false, null);

        assertFalse(message.isPing());
        assertTrue(message.isPong());
        assertFalse(message.isClose());
        assertTrue(message.isEnd());
        assertFalse(message.isFragment());
        assertFalse(message.isString());
        assertFalse(message.isByte());
    }

    @Test
    public void closeMessage() {
        WebSocketMessage message = new WebSocketMessage(WebSocketMessage.Type.CLOSE, WebSocketMessage.DataType.STRING, true, false, null);

        assertFalse(message.isPing());
        assertFalse(message.isPong());
        assertTrue(message.isClose());
        assertTrue(message.isEnd());
        assertFalse(message.isFragment());
        assertFalse(message.isString());
        assertFalse(message.isByte());
    }

    @Test
    public void stringDataType() {
        WebSocketMessage message = new WebSocketMessage(WebSocketMessage.Type.MESSAGE, WebSocketMessage.DataType.STRING, false, true, "test".getBytes(StandardCharsets.UTF_8));

        assertTrue(message.isString());
        assertFalse(message.isByte());
        assertTrue(message.isEnd());
        assertFalse(message.isFragment());
        assertEquals("test", message.getMessageString());
    }

    @Test
    public void byteDataType() {
        WebSocketMessage message = new WebSocketMessage(WebSocketMessage.Type.MESSAGE, WebSocketMessage.DataType.BYTE, false, true, "test".getBytes(StandardCharsets.UTF_8));

        assertFalse(message.isString());
        assertTrue(message.isByte());
        assertTrue(message.isEnd());
        assertFalse(message.isFragment());
        assertArrayEquals("test".getBytes(StandardCharsets.UTF_8), message.getMessage());
    }

    @Test
    public void unknownDataType() {
        WebSocketMessage message = new WebSocketMessage(WebSocketMessage.Type.MESSAGE, WebSocketMessage.DataType.UNKNOWN, false, true, "test".getBytes(StandardCharsets.UTF_8));

        assertFalse(message.isString());
        assertFalse(message.isByte());
        assertTrue(message.isEnd());
        assertFalse(message.isFragment());
    }

    @Test
    public void fragmentMessage() {
        WebSocketMessage message = new WebSocketMessage(WebSocketMessage.Type.MESSAGE, WebSocketMessage.DataType.STRING, true, true, "test".getBytes(StandardCharsets.UTF_8));

        assertTrue(message.isFragment());
    }
}