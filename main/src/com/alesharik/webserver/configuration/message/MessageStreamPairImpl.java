package com.alesharik.webserver.configuration.message;

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class MessageStreamPairImpl<M extends Message> implements MessageStreamPair<M, MessageStreamPairImpl.MessageStreamImpl> {
    private final MessageStreamImpl<M> first;
    private final MessageStreamImpl<M> second;

    public MessageStreamPairImpl() {
        ConcurrentLinkedQueue<M> firstQueue = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<M> secondQueue = new ConcurrentLinkedQueue<>();

        first = new MessageStreamImpl<>(secondQueue, firstQueue);
        second = new MessageStreamImpl<>(firstQueue, secondQueue);
    }

    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    @Nonnull
    @Override
    public MessageStreamImpl first() {
        return first;
    }

    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    @Nonnull
    @Override
    public MessageStreamImpl second() {
        return second;
    }

    @Prefixes({"[MessageStream]"})
    public static final class MessageStreamImpl<M extends Message> implements MessageStream<M> {
        private final ConcurrentLinkedQueue<M> sendQueue;
        private final ConcurrentLinkedQueue<M> receiveQueue;

        public MessageStreamImpl(ConcurrentLinkedQueue<M> sendQueue, ConcurrentLinkedQueue<M> receiveQueue) {
            this.sendQueue = sendQueue;
            this.receiveQueue = receiveQueue;
        }

        @Override
        public void sendMessage(M message) {
            sendQueue.add(message);
        }

        @Nonnull
        @Override
        public M receiveMessage() throws InterruptedException {
            if(Thread.interrupted()) {
                return null;
            }
            while(receiveQueue.peek() == null) {
                Signaller signaller = new Signaller(0L, 0L, receiveQueue);
                waitForSignaller(signaller);
            }
            return receiveQueue.poll();
        }

        @Nullable
        @Override
        public M receiveMessage(long timeout, TimeUnit timeUnit) throws InterruptedException {
            if(Thread.interrupted()) {
                return null;
            }
            long nanos = timeUnit.toNanos(timeout);
            if(receiveQueue.peek() == null) {
                long d = System.nanoTime() + nanos;
                Signaller signaller = new Signaller(nanos, d == 0L ? 1L : d, receiveQueue);
                waitForSignaller(signaller);
            }
            return receiveQueue.poll();
        }

        private void waitForSignaller(Signaller signaller) {
            if(signaller.thread != null && receiveQueue.peek() == null) {
                try {
                    ForkJoinPool.managedBlock(signaller);
                } catch (InterruptedException e) {
                    Logger.log(e);
                    Thread.currentThread().interrupt();
                }
            }
        }

        private static final class Signaller implements ForkJoinPool.ManagedBlocker {
            long nanos;
            final long deadline;
            final Queue value;
            volatile Thread thread;

            Signaller(long nanos, long deadline, Queue value) {
                this.thread = Thread.currentThread();
                this.nanos = nanos;
                this.deadline = deadline;
                this.value = value;
            }

            public boolean block() {
                if(isReleasable()) {
                    return true;
                } else if(nanos > 0L) {
                    LockSupport.parkNanos(this, nanos);
                } else if(deadline == 0L) {
                    LockSupport.park(this);
                }
                return isReleasable();
            }

            @Override
            public boolean isReleasable() {
                if(deadline != 0L && (nanos <= 0L || (nanos = deadline - System.nanoTime()) <= 0L)) {
                    thread = null;
                    return true;
                }
                return value.peek() != null;
            }
        }
    }
}
