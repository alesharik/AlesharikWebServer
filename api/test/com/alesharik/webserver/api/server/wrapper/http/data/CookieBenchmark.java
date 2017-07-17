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

package com.alesharik.webserver.api.server.wrapper.http.data;

import com.alesharik.webserver.benchmark.BenchmarkTest;
import org.glassfish.grizzly.http.util.CookieSerializerUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;

@BenchmarkTest("cookiesTest")
@Measurement(timeUnit = TimeUnit.NANOSECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class CookieBenchmark {
    private Cookie myCookie = new Cookie("test", "test");
    private org.glassfish.grizzly.http.Cookie grassfishCookie = new org.glassfish.grizzly.http.Cookie("test", "test");

    {
        myCookie.setComment("comment");
        myCookie.setDomain(".bar.foo");
        myCookie.setPath("/test");
        myCookie.setMaxAge(1);
        myCookie.setSecure(true);
        myCookie.setVersion(1);
        myCookie.setHttpOnly(true);

        grassfishCookie.setComment("comment");
        grassfishCookie.setDomain(".bar.foo");
        grassfishCookie.setPath("/test");
        grassfishCookie.setMaxAge(1);
        grassfishCookie.setSecure(true);
        grassfishCookie.setVersion(1);
        grassfishCookie.setHttpOnly(true);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("cookies")
    @GroupThreads(2)
    public String myCookieTest() {
        return myCookie.toCookieString();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("cookies")
    @GroupThreads(2)
    public String grassfishCookieTest() {
        StringBuilder stringBuilder = new StringBuilder();
        CookieSerializerUtils.serializeServerCookie(stringBuilder, grassfishCookie);
        return stringBuilder.toString();
    }
}