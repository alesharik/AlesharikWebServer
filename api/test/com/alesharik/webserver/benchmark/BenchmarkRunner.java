package com.alesharik.webserver.benchmark;

import com.alesharik.webserver.logger.Logger;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.io.File;
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
                stop();
            } else {
                options.include(benchmarks.get(benchName).getCanonicalName());
            }
        }


        try {
            new Runner(options.forks(1).build(), new CustomOutputFormat(System.out, VerboseMode.EXTRA)).run();
        } catch (RunnerException e) {
            e.printStackTrace();
        }
    }

    private static void scanBenchmarks() {
        new FastClasspathScanner()
                .overrideClassLoaders(BenchmarkRunner.class.getClassLoader())
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
                case "-l":
                case "--log": {
                    i++;
                    String pathString = args[i];
                    File file = new File(pathString);
                    if(!file.exists()) {
                        if(!file.createNewFile()) {
                            System.err.println("Can't create log file!");
                            stop();
                        }
                    }
                    Logger.setupLogger(file, 10);
                    break;
                }
                case "-h":
                case "--help": {
                    System.out.println("-h, --help                                 show help");
                    System.out.println("-l [file], --log [file]                    log out and err to [file]");
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