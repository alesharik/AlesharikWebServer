package com.alesharik.webserver.api.fileManager;

import com.alesharik.webserver.benchmark.BenchmarkTest;
import com.alesharik.webserver.logger.Logger;
import org.glassfish.grizzly.utils.Charsets;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Measurement(timeUnit = TimeUnit.NANOSECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@BenchmarkTest("FileManager")
public class FileManagerBenchmark {
    private FileManager fileManager;
    private final byte[] TEST_FILE_BYTES = "test".getBytes(Charsets.UTF8_CHARSET);

    @Setup
    public void setupLogger() throws IOException {
        try {
            Logger.setupLogger(File.createTempFile("dsfgsdfdsafasd", "hdgsdfdsgsadf"), 0);
            Logger.disable();
            File root = Files.createTempDirectory("fgdgasdasd").toFile();
            File file = new File(root.getAbsolutePath() + "test.txt");
            if(!file.createNewFile()) {
                throw new IOException();
            }
            Files.write(file.toPath(), TEST_FILE_BYTES);
            fileManager = new FileManager(root, FileManager.FileHoldingMode.HOLD_AND_CHECK);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("read")
    public boolean readTest() {
        return Arrays.equals(fileManager.readFile("text.txt"), TEST_FILE_BYTES);
    }
}