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