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

package com.alesharik.webserver.logger;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.File;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.concurrent.TimeUnit;

@Measurement(timeUnit = TimeUnit.NANOSECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class LoggerBenchmark {

    @Param({"c", "smallText", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."})
    private String logMessage;

    private Throwable throwable = new ConcurrentModificationException();

    @Setup
    public void setupLogger() throws IOException {
        Logger.setupLogger(File.createTempFile("dsfgsdfdsafasd", "hdgsdfdsgsadf"), 0);
        Logger.disable();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("log")
    @GroupThreads(2)
    public void log() throws Exception {
        Logger.log(logMessage);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("log")
    @GroupThreads
    public void log1() throws Exception {
        Logger.log(throwable);
    }

}