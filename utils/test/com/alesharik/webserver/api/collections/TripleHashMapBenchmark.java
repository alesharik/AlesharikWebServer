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

package com.alesharik.webserver.api.collections;

import org.apache.commons.lang3.RandomStringUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@Measurement(timeUnit = TimeUnit.NANOSECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class TripleHashMapBenchmark {
    private TripleHashMap<String, Integer, Integer> map = new TripleHashMap<>();

    private String[] keys = new String[1100];

    private int counter;

    @Setup
    public void setup() {
        map.put("a", 1, 1);
        map.put("b", 2, 3);
        map.put("c", 2, 3);

        for(int i = 0; i < 1200; i++) {
            keys[i] = RandomStringUtils.random(128);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("get")
    @GroupThreads(2)
    public Integer getBenchmark() {
        return map.get("a");
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("get")
    @GroupThreads(2)
    public Integer getAdditionBenchmark() {
        return map.getAddition("a");
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("set")
    @Warmup(iterations = 100)
    @Measurement(iterations = 1000)
    public void add() {
        map.put(keys[counter++], 2, 3);
    }
}