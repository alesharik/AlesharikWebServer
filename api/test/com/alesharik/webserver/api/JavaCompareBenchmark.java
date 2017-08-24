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

import com.alesharik.webserver.benchmark.BenchmarkTest;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@BenchmarkTest("java")
@Measurement(timeUnit = TimeUnit.NANOSECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class JavaCompareBenchmark {
    private static final Method testMethod;
    private static final MethodHandle testMethodHandle;
    private static final Object o = new Object();

    static {
        try {
            testMethod = PrivateObject.class.getDeclaredMethod("method", Object.class);
            testMethod.setAccessible(true);
            testMethodHandle = MethodHandles.lookup().unreflect(testMethod);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new Error(e);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public String reflectionMethodCall() {
        try {
            return (String) testMethod.invoke(null, o);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public String methodHandleArgumentsCall() {
        try {
            return (String) testMethodHandle.invokeWithArguments(o);
        } catch (Throwable e) {
            e.printStackTrace();
            return "";
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public String methodHandleExactCall() {
        try {
            return (String) testMethodHandle.invokeExact(o);
        } catch (Throwable e) {
            e.printStackTrace();
            return "";
        }
    }
}
