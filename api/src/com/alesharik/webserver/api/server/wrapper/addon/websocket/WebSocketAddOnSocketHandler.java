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

import com.alesharik.webserver.api.memory.impl.ByteOffHeapVector;
import com.alesharik.webserver.api.server.wrapper.addon.AddOnSocketContext;
import com.alesharik.webserver.api.server.wrapper.addon.AddOnSocketHandler;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alesharik.webserver.api.server.wrapper.addon.websocket.WebSocketConstants.*;

@RequiredArgsConstructor
final class WebSocketAddOnSocketHandler implements AddOnSocketHandler {//TODO extensions support
    private static final ByteOffHeapVector byteVector = ByteOffHeapVector.instance();
    private final WebSocketMessageProcessor processor;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicBoolean opened = new AtomicBoolean(false);
    private final AtomicBoolean reading = new AtomicBoolean(false);
    private final AtomicBoolean handlingMessageBuffer = new AtomicBoolean(false);
    private final ByteBuffer primitiveBuilder = ByteBuffer.allocate(8);
    private long buffer;

    private volatile boolean writingMessage;
    private ReadingState readingState = ReadingState.NONE;
    private volatile boolean fin;
    private volatile byte opcode;//DO NOT RESET
    private volatile boolean maskEnabled;
    private long length;
    private volatile byte[] mask;
    private long dataCursor;
    private volatile long messageBuffer;

    private boolean isFragmentMessage = false;

    private WebSocketBroadcaster webSocketBroadcaster;

    @Override
    public void init(@Nonnull AddOnSocketContext context) {
        if(opened.get())
            throw new IllegalStateException();
        opened.set(true);
        closed.set(false);
        buffer = byteVector.allocate();
        webSocketBroadcaster = new WebSocketBroadcaster(context);
        processor.setBroadcaster(webSocketBroadcaster);
        processor.setContext(new WebSocketMessageProcessorContext(context.getHandshakeRequest()));
        processor.onConnect();
    }

    @Override
    public void handle(@Nonnull byte[] byteBuffer, @Nonnull AddOnSocketContext context) {
        buffer = byteVector.write(buffer, byteBuffer);
        while(byteVector.size(buffer) > 0 && !(readingState == ReadingState.LENGTH && ((byteVector.size(buffer) < 2 && this.length == 126) || (byteVector.size(buffer) < 8 && this.length == 127))))
            readMessage(context);
    }

    private void readMessage(AddOnSocketContext context) {
        if(buffer == 0) {
            System.err.println("Warning! Try to read message on empty buffer!");
            return;
        }
        reading.set(true);
        if(closed.get()) {
            reading.set(false);
            return;
        }

        if(!writingMessage) {
            writingMessage = true;
            readingState = ReadingState.FIRST_BYTE;
        }
        if(readingState == ReadingState.FIRST_BYTE) {
            byte[] cut = byteVector.cut(buffer, 1);
            byte firstByte = cut[0];
            fin = (firstByte & CUT_FIN) == CUT_FIN;
            byte opcode = (byte) (firstByte & CUT_OPCODE);
            if(opcode == CONTINUE)
                opcode = this.opcode;
            this.opcode = opcode;
            readingState = ReadingState.SECOND_BYTE;
            return;
        }
        if(readingState == ReadingState.SECOND_BYTE) {
            byte[] cut = byteVector.cut(buffer, 1);
            byte second = cut[0];
            maskEnabled = (second & CUT_MASK) == CUT_MASK;
            this.length = (byte) (second & CUT_LENGTH);//set basic length
            if(this.length < 126)
                readingState = ReadingState.MASK;
            else
                readingState = ReadingState.LENGTH;
            return;
        }
        if(readingState == ReadingState.LENGTH && this.length == 126 && byteVector.size(buffer) >= 2) {
            primitiveBuilder.clear();

            primitiveBuilder.put(byteVector.cut(buffer, 2));
            length += primitiveBuilder.getShort();
            readingState = maskEnabled ? ReadingState.MASK : ReadingState.DATA;
            return;
        }
        if(readingState == ReadingState.LENGTH && this.length == 127 && byteVector.size(buffer) >= 8) {
            primitiveBuilder.clear();

            primitiveBuilder.put(byteVector.cut(buffer, 8));
            length += primitiveBuilder.getLong();
            readingState = maskEnabled ? ReadingState.MASK : ReadingState.DATA;
            return;
        }
        if(readingState == ReadingState.MASK && byteVector.size(buffer) >= 4) {
            mask = byteVector.cut(buffer, 4);
            readingState = ReadingState.DATA;
            return;
        }
        if(readingState == ReadingState.DATA) {
            long messageBuffer = this.messageBuffer;
            if(messageBuffer == 0)
                this.messageBuffer = messageBuffer = byteVector.allocate();
            long buffer = this.buffer;
            if(buffer == 0)
                return;
            long last = length - dataCursor;
            int canRead = (int) Math.min(last, byteVector.size(buffer));//Does not support long messages. Use fragmentation
            byte[] read = byteVector.cut(buffer, canRead);
            for(int i = 0; i < read.length; i++)
                read[i] = (byte) (read[i] ^ mask[i % 4]);
            this.messageBuffer = byteVector.write(messageBuffer, read);
            dataCursor += canRead;
            if(dataCursor == length) {
                handlingMessageBuffer.set(true);
                reading.set(false);
                flushMessage(context);
                return;
            }
        }
        reading.set(false);
    }

    private void flushMessage(AddOnSocketContext context) {
        boolean isText = (opcode & TEXT) == TEXT;
        boolean isByte = (opcode & BYTE) == BYTE;
        boolean isClose = (opcode & CLOSE) == CLOSE;
        boolean isPing = (opcode & PING) == PING;
        boolean isPong = (opcode & PONG) == PONG;
        if(processor.enableMessages()) {
            WebSocketMessage.Type type;
            if(isPing)
                type = WebSocketMessage.Type.PING;
            else if(isPong)
                type = WebSocketMessage.Type.PONG;
            else if(isClose)
                type = WebSocketMessage.Type.CLOSE;
            else
                type = WebSocketMessage.Type.MESSAGE;

            WebSocketMessage.DataType dataType;
            if(isText)
                dataType = WebSocketMessage.DataType.STRING;
            else if(isByte)
                dataType = WebSocketMessage.DataType.BYTE;
            else
                dataType = WebSocketMessage.DataType.UNKNOWN;

            WebSocketMessage message = new WebSocketMessage(type, dataType, !fin || isFragmentMessage, fin, byteVector.toByteArray(messageBuffer));
            isFragmentMessage = !fin;
            processor.processMessage(message, webSocketBroadcaster);
        } else {
            if(isPing)
                processor.onPing();
            else if(isPong)
                processor.onPong();
            else if(isClose) {
                primitiveBuilder.clear();
                primitiveBuilder.put(byteVector.toByteArray(messageBuffer));
                int code = primitiveBuilder.getInt();
                processor.onClose(code);
                context.close();
            } else if(!fin || isFragmentMessage) {
                isFragmentMessage = !fin;
                if(isByte)
                    processor.onFragment(fin, byteVector.toByteArray(messageBuffer));
                else if(isText)
                    processor.onFragment(fin, new String(byteVector.toByteArray(messageBuffer), StandardCharsets.UTF_8));
            } else if(fin) {
                if(isByte)
                    processor.onMessage(byteVector.toByteArray(messageBuffer));
                else if(isText)
                    processor.onMessage(new String(byteVector.toByteArray(messageBuffer), StandardCharsets.UTF_8));
            }
        }

        byteVector.free(messageBuffer);
        messageBuffer = 0;
        handlingMessageBuffer.set(false);
        readingState = ReadingState.NONE;
        fin = false;
        maskEnabled = false;
        length = 0;
        mask = new byte[0];
        dataCursor = 0;
        writingMessage = false;
    }

    @Override
    public void close(@Nonnull AddOnSocketContext context) {
        if(!opened.get() || closed.compareAndSet(false, true))
            return;
        while(reading.get()) ;
        processor.onClose(-1);
        byteVector.free(buffer);
        buffer = 0;
        if(!handlingMessageBuffer.get())
            if(messageBuffer != 0)
                byteVector.free(messageBuffer);
    }

    enum ReadingState {
        NONE,
        FIRST_BYTE,
        SECOND_BYTE,
        LENGTH,
        MASK,
        DATA
    }
}
