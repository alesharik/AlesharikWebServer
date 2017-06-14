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
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;

@BenchmarkTest("LoginPasswordCoder")
@Measurement(timeUnit = TimeUnit.NANOSECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class LoginPasswordCoderBenchmark {
    @Param({"hi", "test", "admin", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed tincidunt blandit tellus nec faucibus. Etiam aliquet sapien eu libero hendrerit posuere. Morbi sollicitudin ante sed elit molestie scelerisque. Suspendisse potenti. Nunc sagittis est sit amet odio lacinia semper. Nunc porttitor, ligula non bibendum commodo, leo nisi fermentum lacus, in egestas odio est a lectus. Pellentesque convallis sapien ex, ac ornare diam accumsan venenatis. In hendrerit est id elementum pharetra. Suspendisse potenti. Suspendisse potenti."})
    private String login;
    @Param({"hi", "test", "admin", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed tincidunt blandit tellus nec faucibus. Etiam aliquet sapien eu libero hendrerit posuere. Morbi sollicitudin ante sed elit molestie scelerisque. Suspendisse potenti. Nunc sagittis est sit amet odio lacinia semper. Nunc porttitor, ligula non bibendum commodo, leo nisi fermentum lacus, in egestas odio est a lectus. Pellentesque convallis sapien ex, ac ornare diam accumsan venenatis. In hendrerit est id elementum pharetra. Suspendisse potenti. Suspendisse potenti."})
    private String password;

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("LoginPasswordCoder")
    @GroupThreads(2)
    public String encode() {
        return LoginPasswordCoder.encode(login, password);
    }
}