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

package com.alesharik.webserver.api.serial;

import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Measurement(timeUnit = TimeUnit.NANOSECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class SerialBenchmark {
    private final TestClass testClass = new TestClass();
    private ObjectOutputStream write;
    private ObjectInputStream read;
    private PipedOutputStream out;
    private PipedInputStream in;

    @Setup
    @SneakyThrows
    public void setup() {
        out = new PipedOutputStream();
        in = new PipedInputStream(out);
        write = new ObjectOutputStream(out);
        read = new ObjectInputStream(in);
        write.writeObject(testClass);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("Serial")
    public TestClass serial() {
        byte[] serialize = Serial.serialize(testClass);
        return Serial.deserialize(serialize);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("JavaSerial")
    public TestClass javaSerial() {
        try {
            write.writeObject(testClass);
            return (TestClass) read.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @EqualsAndHashCode
    private static final class TestClass implements Serializable {
        private final String a = "test";
        private final int b = 12;
        private final Test1 test1 = new Test1();
    }

    @EqualsAndHashCode
    private static final class Test1 implements Serializable {
        private final String a = "qw";
        private final long b = 12L;
    }
}
