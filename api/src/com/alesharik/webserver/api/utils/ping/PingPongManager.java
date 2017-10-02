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

package com.alesharik.webserver.api.utils.ping;

import com.alesharik.webserver.api.ThreadFactories;
import com.alesharik.webserver.api.cache.object.Recyclable;
import com.alesharik.webserver.api.cache.object.SmartCachedObjectFactory;
import com.alesharik.webserver.api.utils.Constants;
import com.alesharik.webserver.api.utils.lambda.Action;
import com.alesharik.webserver.api.utils.lambda.LambdaUtils;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Ping-Pong is basic keep-alive system. Every ping has it's own ID, which is unique for running application. If ping has no pong in <code>timeout</code>
 * time, it wil be cleared with firing <code>timeout</code> action. If ping has pon, it will be deleted without notifying anyone
 */
@UtilityClass
public class PingPongManager {
    private static final long DEFAULT_TIMEOUT = TimeUnit.SECONDS.toMillis(5);
    private static final ThreadGroup THREAD_GROUP = new ThreadGroup(Constants.UTILS_THREAD_GROUP, "PingPong");
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), ThreadFactories.newThreadFactory(THREAD_GROUP));

    static final PingPong GLOBAL = new PingPongImpl();

    /**
     * Create new {@link PingPong}
     *
     * @return new {@link PingPong}
     */
    @Nonnull
    public static PingPong newPingPong() {
        return new PingPongImpl();
    }

    /**
     * Add pingable to scheduler. Use global PingPong group
     *
     * @param pingable the pingable
     * @param time     ping time period
     * @param unit     ping time time unit
     * @return PingableScheduler for pingable
     */
    @Nonnull
    public static PingableScheduler schedulePingable(@Nonnull Pingable pingable, long time, @Nonnull TimeUnit unit) {
        return schedulePingable(pingable, GLOBAL, time, unit);
    }

    /**
     * Add pingable to scheduler
     *
     * @param pingable the pingable
     * @param pingPong PingPong group to use
     * @param time     ping time period
     * @param unit     ping time time unit
     * @return PingableScheduler for pingable
     */
    @Nonnull
    public static PingableScheduler schedulePingable(@Nonnull Pingable pingable, @Nonnull PingPong pingPong, long time, @Nonnull TimeUnit unit) {
        PingableSchedulerImpl pingableScheduler = new PingableSchedulerImpl(pingable, pingPong);
        ScheduledFuture<?> future = EXECUTOR_SERVICE.scheduleAtFixedRate(pingableScheduler, 0, time, unit);
        pingableScheduler.setFuture(future);
        return pingableScheduler;
    }

    /**
     * Ping from global PingPong group
     */
    public static long ping() {
        return GLOBAL.ping();
    }

    /**
     * Pong to global PingPong group
     */
    public static void pong(long l) {
        GLOBAL.pong(l);
    }

    /**
     * PingTimeout of global PingPong group
     */
    public static Action<Long, PingPong> pingTimeout() {
        return GLOBAL.pingTimeout();
    }

    static final class PingPongImpl implements PingPong {
        final List<Long> pings = new CopyOnWriteArrayList<>();
        final Action<Long, PingPong> timeoutAction = LambdaUtils.action(Long.class, this);

        @Setter
        private volatile long timeout = DEFAULT_TIMEOUT;

        @Override
        public long ping() {
            long p = SequenceGenerator.get();
            pings.add(p);
            EXECUTOR_SERVICE.schedule(ExpireTask.newInstance(pings, timeoutAction, p), timeout, TimeUnit.MILLISECONDS);
            return p;
        }

        @Override
        public void pong(long id) {
            pings.remove(id);
        }

        @Override
        public Action<Long, PingPong> pingTimeout() {
            return timeoutAction;
        }

        static final class ExpireTask implements Runnable, Recyclable {
            private static final SmartCachedObjectFactory<ExpireTask> FACTORY = new SmartCachedObjectFactory<>(ExpireTask::new);

            private volatile List<Long> pings;
            private volatile Action<Long, PingPong> time;
            private volatile long pingId;

            private ExpireTask() {
            }

            public static ExpireTask newInstance(List<Long> pings, Action<Long, PingPong> time, long pingId) {
                ExpireTask timerTask = FACTORY.getInstance();
                timerTask.pings = pings;
                timerTask.pingId = pingId;
                timerTask.time = time;
                return timerTask;
            }

            @Override
            public void run() {
                if(pings.contains(pingId)) {
                    time.call(pingId);
                    pings.remove(pingId);
                }
                FACTORY.putInstance(this);
            }

            @Override
            public void recycle() {
                pings = null;
                time = null;
                pingId = -1;
            }
        }
    }

    /**
     * Generates infinite sequence. If pointer > {@link Long#MAX_VALUE}, when it set to 0
     */
    static final class SequenceGenerator {
        private static final AtomicLong pointer = new AtomicLong(0);

        public static long get() {
            long value;
            long n;
            do {
                value = pointer.get();
                n = value + 1;
                if(n > Long.MAX_VALUE)
                    n = 0;
            } while(!pointer.compareAndSet(value, n));
            return n;
        }
    }

    private static final class PingableSchedulerImpl implements PingableScheduler, Runnable {
        private final Pingable pingable;
        private final PingPong pingPong;
        @Setter
        private volatile ScheduledFuture<?> future;

        public PingableSchedulerImpl(Pingable pingable, PingPong pingPong) {
            this.pingable = pingable;
            this.pingPong = pingPong;
        }

        @Override
        public void stop() {
            future.cancel(false);
        }

        @Override
        public void run() {
            if(future == null)
                return;

            try {
                pingable.ping(pingPong.ping());
            } catch (Exception e) {
                future.cancel(true);
                e.printStackTrace();
            }
        }
    }
}
