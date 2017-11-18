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

package com.alesharik.webserver.benchmark;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ForkJoinPool;

/**
 * This class runs all benchmarks
 */
public final class BenchmarkRunner {
    private static final int PARALLELISM = Runtime.getRuntime().availableProcessors();

    private static final HashMap<String, Class<?>> benchmarks = new HashMap<>();
    private static final ForkJoinPool pool = new ForkJoinPool(PARALLELISM);

    private static boolean runAll = true;
    private static String benchName;

    public static void main(String[] args) throws IOException {
        scanBenchmarks();
        parseArgs(args);
        OptionsBuilder options = new OptionsBuilder();

        if(runAll) {
            System.out.println("Running all benchmarks");
            benchmarks.forEach((s, aClass) -> options.include(aClass.getCanonicalName()));
        } else {
            if(!benchmarks.containsKey(benchName)) {
                System.out.println("Don't have benchmark " + benchName);
//                stop();
            } else {
                options.include(benchmarks.get(benchName).getCanonicalName());
            }
        }
        options.include("com.alesharik.webserver.api.collections.TripleHashMapBenchmark");


        try {
            new Runner(options.forks(1).build(), new CustomOutputFormat(System.out, VerboseMode.EXTRA)).run();
        } catch (RunnerException e) {
            e.printStackTrace();
        }
    }

    private static void scanBenchmarks() {
        new FastClasspathScanner()
                .matchClassesWithAnnotation(BenchmarkTest.class, BenchmarkRunner::addBenchmark)
                .scan(pool, PARALLELISM);
    }

    private static void addBenchmark(Class<?> benchmark) {
        BenchmarkTest annotation = benchmark.getAnnotation(BenchmarkTest.class);
        benchmarks.put(annotation.value(), benchmark);
    }

    private static void parseArgs(String[] args) throws IOException {
        for(int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-h":
                case "--help": {
                    System.out.println("-h, --help                                 show help");
                    System.out.println("-b [benchmark], --benchmark [benchmark]    run benchmark");
                    break;
                }
                case "-b":
                case "--benchmark": {
                    i++;
                    benchName = args[i];
                    runAll = false;
                    break;
                }
                default:
                    System.out.println("WTF! Argument not expected!");
                    break;
            }
        }
    }

    private static void stop() {
        pool.shutdown();
        System.exit(0);
    }
}
