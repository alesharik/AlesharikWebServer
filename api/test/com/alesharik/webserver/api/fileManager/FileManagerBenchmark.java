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