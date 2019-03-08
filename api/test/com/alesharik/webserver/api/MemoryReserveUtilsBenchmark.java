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

package com.alesharik.webserver.api;

import com.alesharik.webserver.internals.MemoryReserveUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

@Measurement(timeUnit = TimeUnit.NANOSECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MemoryReserveUtilsBenchmark {//Benchmarks API located in api module

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void reserveUnreserveMemory() throws Exception {
        MemoryReserveUtils.reserveMemory(1024);
        MemoryReserveUtils.unreserveMemory(1024);
    }
}