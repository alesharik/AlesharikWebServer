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

package com.alesharik.webserver.module.http.util;

import com.alesharik.webserver.module.http.http.HttpStatus;
import com.alesharik.webserver.module.http.http.Response;
import org.junit.Before;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static com.alesharik.webserver.test.http.HttpMockUtils.*;
import static org.junit.Assert.*;

public class IpBanManagerTest {
    private IpBanManager ipBanManager;

    @Before
    public void setUp() throws Exception {
        ipBanManager = new IpBanManager(1, TimeUnit.MILLISECONDS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithMicroseconds() {
        new IpBanManager(1, TimeUnit.MICROSECONDS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNanoseconds() {
        new IpBanManager(1, TimeUnit.NANOSECONDS);
    }

    @Test
    public void global() {
        assertNotNull(IpBanManager.global());
    }

    @Test
    public void ban() throws Exception {
        InetSocketAddress socketAddress = new InetSocketAddress("4.4.4.4", 1);

        Response response = response();

        assertTrue(ipBanManager.filter(request()
                .withLocal(InetAddress.getByName("localhost"))
                .withRemote(socketAddress)
                .setSecure(true)
                .build(), response));

        ipBanManager.ban(socketAddress.getAddress());

        response = response();
        assertFalse(ipBanManager.filter(request()
                .withLocal(InetAddress.getByName("localhost"))
                .withRemote(socketAddress)
                .setSecure(true)
                .build(), response));
        verify(response).respond(HttpStatus.TOO_MANY_REQUESTS_429);
    }

    @Test
    public void banWithTime() throws Exception {
        InetSocketAddress socketAddress = new InetSocketAddress("4.4.4.4", 1);

        Response response = response();

        assertTrue(ipBanManager.filter(request()
                .withLocal(InetAddress.getByName("localhost"))
                .withRemote(socketAddress)
                .setSecure(true)
                .build(), response));

        ipBanManager.ban(socketAddress.getAddress(), 10, TimeUnit.MILLISECONDS);

        response = response();
        assertFalse(ipBanManager.filter(request()
                .withLocal(InetAddress.getByName("localhost"))
                .withRemote(socketAddress)
                .setSecure(true)
                .build(), response));

        verify(response).respond(HttpStatus.TOO_MANY_REQUESTS_429);

        Thread.sleep(20);

        assertTrue(ipBanManager.filter(request()
                .withLocal(InetAddress.getByName("localhost"))
                .withRemote(socketAddress)
                .setSecure(true)
                .build(), response));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBanWithTimeMicroseconds() throws Exception {
        ipBanManager.ban(Inet4Address.getByName("localhost"), 1, TimeUnit.MICROSECONDS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBanWithTimeNanoseconds() throws Exception {
        ipBanManager.ban(Inet4Address.getByName("localhost"), 1, TimeUnit.NANOSECONDS);
    }
}