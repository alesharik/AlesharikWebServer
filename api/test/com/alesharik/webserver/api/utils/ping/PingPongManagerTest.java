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

import com.alesharik.webserver.api.TestUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.internal.exceptions.Reporter;
import org.mockito.internal.reporting.Discrepancy;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.verification.VerificationMode;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.alesharik.webserver.api.utils.lambda.LambdaUtils.when;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PingPongManagerTest {
    private PingPongManager.PingPongImpl pingPong;

    @Before
    public void setUp() throws Exception {
        pingPong = (PingPongManager.PingPongImpl) PingPongManager.newPingPong();
    }

    @Test
    public void testPingPong() throws Exception {
        assertTrue(pingPong.pings.isEmpty());

        long ping = pingPong.ping();
        assertFalse(pingPong.pings.isEmpty());
        assertEquals(ping, pingPong.pings.get(0).longValue());

        pingPong.pong(ping);
        assertTrue(pingPong.pings.isEmpty());
    }

    @Test
    public void testPingExpired() throws Exception {
        pingPong.setTimeout(5, TimeUnit.MILLISECONDS);

        Consumer consumer = mock(Consumer.class);
        //noinspection unchecked
        when(pingPong.pingTimeout()).then(consumer);

        assertTrue(pingPong.pings.isEmpty());

        long ping = pingPong.ping();
        assertFalse(pingPong.pings.isEmpty());
        assertEquals(ping, pingPong.pings.get(0).longValue());

        Thread.sleep(10);

        //noinspection unchecked
        verify(consumer, times(1)).accept(ping);
    }

    @Test
    public void testPingPongGlobal() throws Exception {
        long l = PingPongManager.ping();
        PingPongManager.pong(l);
    }

    @Test
    public void testGlobalPingExpired() throws Exception {
        PingPongManager.GLOBAL.setTimeout(1, TimeUnit.MILLISECONDS);

        Consumer consumer = mock(Consumer.class);
        //noinspection unchecked
        when(PingPongManager.pingTimeout()).then(consumer);

        ((PingPongManager.PingPongImpl) PingPongManager.GLOBAL).pings.clear();
        assertTrue(((PingPongManager.PingPongImpl) PingPongManager.GLOBAL).pings.isEmpty());

        long ping = PingPongManager.ping();
        assertFalse(((PingPongManager.PingPongImpl) PingPongManager.GLOBAL).pings.isEmpty());
        assertEquals(ping, ((PingPongManager.PingPongImpl) PingPongManager.GLOBAL).pings.get(0).longValue());

        Thread.sleep(10);

        //noinspection unchecked
        verify(consumer, times(1)).accept(ping);
    }

    @Test
    public void testPingable() throws Exception {
        Pingable pingable = mock(Pingable.class);
        PingableScheduler scheduler = PingPongManager.schedulePingable(pingable, 1, TimeUnit.MILLISECONDS);
        Thread.sleep(2);
        scheduler.stop();

        verify(pingable, new VerificationMode() {
            @Override
            public void verify(VerificationData data) {
                if(data.getAllInvocations().size() > 4)
                    throw Reporter.tooManyActualInvocations(4, data.getAllInvocations().size(), data.getTarget(), data.getTarget().getLocation());
                else if(data.getAllInvocations().size() < 1)
                    throw Reporter.tooLittleActualInvocations(new Discrepancy(1, data.getAllInvocations().size()), data.getTarget(), data.getTarget().getLocation());
            }

            @Override
            public VerificationMode description(String description) {
                return this;
            }
        }).ping(anyLong());
    }

    @Test
    public void testPingableWithException() throws Exception {
        Pingable pingable = mock(Pingable.class);
        RuntimeException exception = mock(RuntimeException.class);
        doThrow(exception).when(pingable).ping(anyLong());

        PingPongManager.schedulePingable(pingable, 1, TimeUnit.MILLISECONDS);
        Thread.sleep(10);

        verify(pingable, only()).ping(anyLong());
        verify(exception, only()).printStackTrace();
    }

    @Test
    public void testUtility() throws Exception {
        TestUtils.assertUtilityClass(PingPongManager.class);
    }

    @Test
    @Ignore
    public void testSequenceGenerator() throws Exception {
        PingPongManager.SequenceGenerator.get();
        for(long i = 0; i < Long.MAX_VALUE; i++) {
            PingPongManager.SequenceGenerator.get();
        }
        assertTrue(Long.MAX_VALUE > PingPongManager.SequenceGenerator.get());
    }
}