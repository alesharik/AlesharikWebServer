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

import com.alesharik.webserver.api.cache.object.CachedObjectFactory;
import com.alesharik.webserver.api.cache.object.Recyclable;
import com.alesharik.webserver.api.cache.object.SmartCachedObjectFactory;
import com.alesharik.webserver.api.memory.impl.ByteOffHeapVector;
import com.alesharik.webserver.module.http.addon.AddOnSocketContext;
import com.alesharik.webserver.module.http.addon.AddOnSocketHandler;
import com.alesharik.webserver.module.http.addon.MessageProcessor;
import com.alesharik.webserver.module.http.addon.websocket.processor.WebSocketMessage;
import com.alesharik.webserver.module.http.addon.websocket.processor.WebSocketMessageProcessor;
import com.alesharik.webserver.module.http.addon.websocket.processor.WebSocketMessageProcessorContext;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alesharik.webserver.module.http.addon.websocket.impl.WebSocketConstants.*;

@RequiredArgsConstructor
final class WebSocketAddOnSocketHandler implements AddOnSocketHandler {
    private static final int MAX_MESSAGE_LENGTH;

    static {
        if(System.getProperty("module.http.addon.websocket.MAX_MESSAGE_LENGTH") != null)
            MAX_MESSAGE_LENGTH = Integer.parseInt(System.getProperty("module.http.addon.websocket.MAX_MESSAGE_LENGTH"));
        else
            MAX_MESSAGE_LENGTH = 32 * 1024 * 1024;
    }

    private final WebSocketMessageProcessor processor;

    @Override
    public void init(@Nonnull AddOnSocketContext context) {
        Ctx ctx = Ctx.create(processor, new WebSocketBroadcaster(context), new WebSocketMessageProcessorContext(context.getHandshakeRequest()));
        ctx.open();
        context.setParameter("context", ctx);
    }

    @Override
    public void handle(@Nonnull ByteBuffer byteBuffer, @Nonnull AddOnSocketContext context) {
        Ctx ctx = (Ctx) context.getParameter("context");
        ctx.handleData(byteBuffer);
    }

    @Override
    public void close(@Nonnull AddOnSocketContext context) {
        Ctx ctx = (Ctx) context.getParameter("context");
        ctx.close();
        Ctx.recycle(ctx);
    }


    private static final class Ctx implements Recyclable {
        private static final CachedObjectFactory<Ctx> FACTORY = new SmartCachedObjectFactory<>(Ctx::new);
        private static final ByteOffHeapVector vector = ByteOffHeapVector.instance();
        private static final ThreadLocal<ByteBuffer> primitiveBuilder = ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(8));

        private final AtomicBoolean closed = new AtomicBoolean();
        private final AtomicBoolean reading = new AtomicBoolean();

        private MessageProcessor<WebSocketMessage, WebSocketBroadcaster, WebSocketMessageProcessorContext> processor;
        private WebSocketBroadcaster broadcaster;
        private WebSocketMessageProcessorContext context;
        private long buffer;

        //====================State machine====================\\
        private ReadingState state;
        private boolean fin;
        private byte opcode;
        private boolean maskEnabled;
        private int length;
        private byte[] mask;
        private boolean fragmented;

        public static Ctx create(MessageProcessor<WebSocketMessage, WebSocketBroadcaster, WebSocketMessageProcessorContext> processor, WebSocketBroadcaster broadcaster, WebSocketMessageProcessorContext context) {
            Ctx ctx = FACTORY.getInstance();
            ctx.processor = processor;
            ctx.broadcaster = broadcaster;
            ctx.context = context;
            ctx.buffer = vector.allocate();
            ctx.state = ReadingState.NONE;
            return ctx;
        }

        public static void recycle(Ctx ctx) {
            FACTORY.putInstance(ctx);
        }

        @Override
        public void recycle() {
            processor = null;
            broadcaster = null;
            context = null;
            closed.set(false);
            reading.set(false);
            ByteOffHeapVector.instance().free(buffer);
            buffer = -1;

            state = ReadingState.NONE;
            fin = false;
            opcode = -1;
            maskEnabled = false;
            length = -1;
            mask = null;
            fragmented = false;
        }

        public void open() {
            if(closed.get())
                throw new IllegalStateException("Closed");
            WebSocketMessage message = WebSocketMessage.create(WebSocketMessage.Type.CONNECT, WebSocketMessage.DataType.BYTE, false, false, new byte[0]);
            processor.processMessage(message, broadcaster, context);
        }

        public void close() {
            //noinspection StatementWithEmptyBody wait for read
            while(reading.get()) ;
            if(closed.compareAndSet(false, true))
                return;
            publish();
            byte[] data = Integer.toString(WebSocketMessage.CloseReason.NONE.getCode()).getBytes(StandardCharsets.UTF_8);
            WebSocketMessage message = WebSocketMessage.create(WebSocketMessage.Type.CLOSE, WebSocketMessage.DataType.STRING, false, false, data);
            processor.processMessage(message, broadcaster, context);
            broadcaster.close();
        }

        @SuppressWarnings("StatementWithEmptyBody")
        public void handleData(ByteBuffer data) {
            if(closed.get())
                throw new IllegalStateException("Closed");

            while(!reading.compareAndSet(false, true))
                while(reading.get()) ;

            byte[] buf = new byte[Math.min(data.remaining(), MAX_MESSAGE_LENGTH + 14)]; //14 - maximum size of meta information
            int lastRemaining = 0;
            while(data.hasRemaining()) {
                lastRemaining = data.remaining();
                data.get(buf);
                vector.write(buffer, buf);
                while(canDoMove())
                    publish();
            }
            if(lastRemaining > 0) {
                vector.write(buffer, buf, 0, lastRemaining);
                while(canDoMove())
                    publish();
            }
            reading.set(false);
        }

        private boolean canDoMove() {
            switch (state) {
                case NONE:
                    return true;
                case FIRST_BYTE:
                case SECOND_BYTE:
                    return vector.size(buffer) > 0;
                case MASK:
                    return vector.size(buffer) >= 4;
                case DATA:
                    return vector.size(buffer) >= length;
                case LENGTH:
                    return length == 126 ? vector.size(buffer) >= 2 : vector.size(buffer) >= 8;
                default:
                    throw new IllegalArgumentException("Unknown state: " + state.name());
            }
        }

        private void publish() {
            if(state == ReadingState.NONE)
                state = ReadingState.FIRST_BYTE;
            if(state == ReadingState.FIRST_BYTE && vector.size(buffer) > 0) {
                byte cut = vector.cut(buffer, 1)[0];
                fin = (byte) (cut & CUT_FIN) == CUT_FIN;//Here you need to handle extensions
                byte opcode = (byte) (cut & CUT_OPCODE);
                if(opcode != CONTINUE)
                    this.opcode = opcode;
                state = ReadingState.SECOND_BYTE;
            }
            if(state == ReadingState.SECOND_BYTE && vector.size(buffer) > 0) {
                byte cut = vector.cut(buffer, 1)[0];
                maskEnabled = (cut & CUT_MASK) == CUT_MASK;
                length = cut & CUT_LENGTH;
                if(length < 126)
                    state = ReadingState.MASK;
                else
                    state = ReadingState.LENGTH;
            }
            if(state == ReadingState.LENGTH && length == 126 && vector.size(buffer) >= 2) {
                ByteBuffer builder = primitiveBuilder.get();
                builder.clear();
                builder.put(vector.cut(buffer, 2));
                length += builder.getShort();
                state = maskEnabled ? ReadingState.MASK : ReadingState.DATA;
            }
            if(state == ReadingState.LENGTH && length == 127 && vector.size(buffer) >= 8) {
                ByteBuffer builder = primitiveBuilder.get();
                builder.clear();
                builder.put(vector.cut(buffer, 8));
                length += builder.getLong();
                if(length > MAX_MESSAGE_LENGTH) {
                    System.err.println("Max message length error! Context: " + context);
                    broadcaster.close(WebSocketMessage.CloseReason.TOO_BIG_1009);
                    return;
                }
                state = maskEnabled ? ReadingState.MASK : ReadingState.DATA;
            }
            if(state == ReadingState.MASK && vector.size(buffer) >= 4) {
                mask = vector.cut(buffer, 4);
                state = ReadingState.DATA;
            }
            if(state == ReadingState.DATA && vector.size(buffer) >= length) {
                byte[] msg = vector.cut(buffer, length);
                if(maskEnabled)
                    for(int i = 0; i < msg.length; i++)
                        msg[i] = (byte) (msg[i] ^ mask[i % 4]);
                flush(msg);
                state = ReadingState.NONE;
            }
        }

        private void flush(byte[] data) {
            boolean isText = (opcode & TEXT) == TEXT;
            boolean isByte = (opcode & BYTE) == BYTE;
            boolean isClose = (opcode & CLOSE) == CLOSE;
            boolean isPing = (opcode & PING) == PING;
            boolean isPong = (opcode & PONG) == PONG;
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
            WebSocketMessage message = WebSocketMessage.create(type, dataType, !fin || fragmented, fin, data);
            fragmented = !fin;
            processor.processMessage(message, broadcaster, context);
            WebSocketMessage.recycle(message);
        }

        private enum ReadingState {
            NONE,
            FIRST_BYTE,
            SECOND_BYTE,
            LENGTH,
            MASK,
            DATA
        }
    }
}
