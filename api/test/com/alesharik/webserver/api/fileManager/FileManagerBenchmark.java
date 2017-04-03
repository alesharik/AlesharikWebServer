package com.alesharik.webserver.api.fileManager;

import com.alesharik.webserver.benchmark.BenchmarkTest;
import org.glassfish.grizzly.utils.Charsets;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Measurement(timeUnit = TimeUnit.NANOSECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@BenchmarkTest("FileManager")
public class FileManagerBenchmark {

    private static File root;
    private static final FileManager fileManager;

    public static final byte[] TEST_FILE_BYTES = "test".getBytes(Charsets.UTF8_CHARSET);

    static {
        try {
            root = Files.createTempDirectory("fgdgasdasd").toFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            File file = new File(root.getAbsolutePath() + "/test.txt");
            if(!file.createNewFile()) {
                throw new Error();
            }
            Files.write(file.toPath(), TEST_FILE_BYTES, StandardOpenOption.DELETE_ON_CLOSE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileManager = new FileManager(root, FileManager.FileHoldingMode.HOLD_AND_CHECK);
    }


    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("read")
    @GroupThreads(2)
    public void readTest() {
        assert Arrays.equals(fileManager.readFile("text.txt"), TEST_FILE_BYTES);
    }
}