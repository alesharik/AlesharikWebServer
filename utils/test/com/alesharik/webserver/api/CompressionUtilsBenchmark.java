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
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;

@BenchmarkTest("CompressionUtils")
@Measurement(timeUnit = TimeUnit.MICROSECONDS)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class CompressionUtilsBenchmark {

    private byte[] base;
    private byte[] compressed;

    @Setup
    public void setup() throws IOException {
        base = Base64.getDecoder().decode("CpcHqcGyUMQIdrSgxOUQif4qpbMHAIKBR6iIJ/JchHI92FJbbR+JEfO6lhF7ARNTRGhJLsRCEWAvObDiiVvg251RLbA8HDr+8kVaSB8YOXvAqyXCT7wTJ67XfmBQnoMyKId43tLEfjTu+6Qc+oEYtJpzVnccxKBHx2mrqK14MKIgINRO9CoSOTuzZptLHd0UC6VgSmpLbhKHaatJAz6o2lXf3jhyuUhlFty4bjsNNPuEjaaY/y9H5O5+1NlV9PUc3Rqt4luj+lNr+l8npm42gQ4wR1wCyuQmv8joIVZqUeH6NcZR9vJ4/EQstPwuiSBWgGFV992VDrIJHZYyJjzO9h0BvsJhw2CK8LMLLJdrGpT2MoZZTLWF3DsDD6OEzOU8+fdk+aIh4l7v6yX7lt8e9EtIfYN2azvus4B/3YOQdnqF1QGDjQGAYWvxUKQ7S+iQ0fm/YBOfj1wFwHOPBV14+YROFpou6Zjc63FsEDKM5IXa5vClOzudvh+PwUAV360VqpuDCPlVOImskK0rVT4DaHTfpEM8Y5isn+/Am24PfYJAUs8Tchren3HSWm4LaoifkYkD2upZyDiHzEG0sUNbCS3CG1aQRY0NqvLOx9T7OSvpjyhwfNbNB8cn/ya8HThVgPm84DvjPk5+0+xFbjPPS8oU1yk/Vy2LyCbeiuNhNLuuknDAC1XGPqSzTXRs3e/qYjQrwxMSgJVObowz1TKC11ntYs1ZtaqXlp/IHmATZzn1t7js0dcqgi4p89rHQYcxlRBQy3OXU8nodH/5e4Z87PRe3tO0YxMppXZiR74KQMLioAZiWgtEmj8iyOfZ+9ikJovZkQ7NCM6QTm4/Dmv2oHGzv1XxP0sYK3gkMePM2m7+LaCx2vYKUgtt8WtjJ4rfEaG8R+ilF5vn9k7lzf/mry41bdpiBh8na/2TkeGGI8Cwg2J4lUPFmbn85M2OFDm5wJ53JyF0oPcfUWo+Dqm89uPf+akwsvIDva4p3/DPwLZd/otNDLGJXaYDL9be60PiXYjuuIhccJkph4e/OByGL+QB44+CYYUUzDiPbyO8gHM1kN2iqpRkwUXedPI9Mhu4/jiDkCueDe4EuITL0AhCzteIvJUGp0q3NEsWAsVlRhIRwfGbrHajx6gD/w5APlmFJO9KLYSEvA01TzgjZb+r5MriKjrVIOXnSa54/uMt2GBPWY7PV2bCgF3Vb91fHjs+Qv6ry//o1mKywoxWfCOirX9SL90mrvFtyrT+P7RcfQRkvJpxG5y0/q1xi5d4EDDZWD76LL/gyv7Kckjb+hxzc2aBspfibXg7NZZOFLm2hXfAZToU5RbniHNLFZKS6qkRgIlT195boprYa8ZhTEYy3w==");
        compressed = CompressionUtils.deflateCompress(base, CompressionUtils.CompressLevel.DEFAULT_COMPRESSION);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Group("CompressionUtils")
    @GroupThreads(2)
    public byte[] testCompressDefault() throws IOException {
        return CompressionUtils.deflateCompress(base, CompressionUtils.CompressLevel.DEFAULT_COMPRESSION);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Group("CompressionUtils")
    @GroupThreads(2)
    public byte[] testCompressFast() throws IOException {
        return CompressionUtils.deflateCompress(base, CompressionUtils.CompressLevel.BEST_SPEED);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Group("CompressionUtils")
    @GroupThreads(2)
    public byte[] testCompressGood() throws IOException {
        return CompressionUtils.deflateCompress(base, CompressionUtils.CompressLevel.BEST_COMPRESSION);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Group("CompressionUtils")
    @GroupThreads(2)
    public byte[] testCompressNoCompress() throws IOException {
        return CompressionUtils.deflateCompress(base, CompressionUtils.CompressLevel.NO_COMPRESSION);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Group("CompressionUtils")
    @GroupThreads(2)
    public byte[] testDecompress() throws DataFormatException, IOException {
        return CompressionUtils.deflateDecompress(compressed);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Group("CompressionUtils")
    @GroupThreads(2)
    public byte[] testCompressAndDecompress() throws IOException, DataFormatException {
        byte[] compressed = CompressionUtils.deflateCompress(base);
        return CompressionUtils.deflateDecompress(compressed);
    }
}
