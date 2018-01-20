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

import com.alesharik.webserver.api.qt.QDataStream;
import com.alesharik.webserver.api.server.wrapper.addon.AddOnSocketContext;
import com.alesharik.webserver.api.server.wrapper.addon.MessageSender;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static com.alesharik.webserver.api.server.wrapper.addon.websocket.WebSocketConstants.*;

public class WebSocketBroadcaster implements MessageSender<WebSocketMessage> {
    private static final ThreadLocal<QDataStream> dataStreams = ThreadLocal.withInitial(QDataStream::new);
    private static final ThreadLocal<ByteArrayOutputStream> buffers = ThreadLocal.withInitial(() -> new ByteArrayOutputStream(4096));
    private final AddOnSocketContext context;

    public WebSocketBroadcaster(AddOnSocketContext context) {
        this.context = context;
    }

    @Override
    public void sendMessage(@Nonnull WebSocketMessage webSocketMessage) {
        if(webSocketMessage.isClose())
            close();
        else if(webSocketMessage.isPing())
            sendPing();
        else if(webSocketMessage.isPong())
            sendPong();
        else if(webSocketMessage.isFragment()) {
            if(webSocketMessage.isString())
                sendFragment(webSocketMessage.isEnd(), true, webSocketMessage.getMessageString());
            else
                sendFragment(webSocketMessage.isEnd(), true, webSocketMessage.getMessage());
        } else {
            if(webSocketMessage.isString())
                sendMessage(webSocketMessage.getMessageString());
            else
                sendMessage(webSocketMessage.getMessage());
        }
    }

    public void sendPing() {
        sendMessage(true, PING, 0, null);
    }

    public void sendPong() {
        sendMessage(true, PONG, 0, null);
    }

    public void sendMessage(String message) {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        sendMessage(true, TEXT, data.length, data);
    }

    public void sendMessage(byte[] message) {
        sendMessage(true, BYTE, message.length, message);
    }

    public void sendFragment(boolean last, boolean first, byte[] fragment) {
        byte opcode = first ? BYTE : CONTINUE;
        sendMessage(last, opcode, fragment.length, fragment);
    }

    public void sendFragment(boolean last, boolean first, String fragment) {
        byte opcode = first ? BYTE : CONTINUE;
        byte[] data = fragment.getBytes(StandardCharsets.UTF_8);
        sendMessage(last, opcode, data.length, data);
    }

    public void close() {
        byte[] c = Integer.toString(1000).getBytes(StandardCharsets.UTF_8);
        sendMessage(true, CLOSE, c.length, c);
    }

    public void close(int code) {//FIXME use enums
        byte[] c = Integer.toString(code).getBytes(StandardCharsets.UTF_8);
        sendMessage(true, CLOSE, c.length, c);
    }

    private void sendMessage(boolean fin, byte opcode, long length, byte[] data) {//server can't mask any frames
        QDataStream stream = dataStreams.get();
        try {
            int size = 1;
            int first = fin ? CUT_FIN : 0;
            first |= opcode;
            stream.writeByte(first);
            int second = 0;
            if(length < 125) {
                second |= length;
                stream.writeByte(second);
                size++;
            } else if(length < 65536) {
                second |= 126;
                stream.writeByte(second);
                stream.writeUnsignedShort((int) (length - 126));
                size += 3;
            } else {
                second |= 127;
                stream.writeByte(second);
                stream.writeUnsignedLong(length);
                size += 9;
            }
            byte[] header = new byte[size];//Can't overflow
            stream.readFully(header);
            context.writeBytes(header);
        } finally {
            stream.recycle();
        }
        if(length == 0)
            return;
        //Write data:
        ByteArrayOutputStream buffer = buffers.get();//FIXME remove
        try {
            int nRead = 0;
            while(nRead < data.length) {
                int read = Math.min(1024, data.length - nRead);
                for(int i = 0; i < read; i++)
                    buffer.write(data[nRead + i]);
                context.writeBytes(buffer.toByteArray());
                buffer.reset();
                nRead += read;
            }
        } finally {
            buffer.reset();
        }
    }
}
