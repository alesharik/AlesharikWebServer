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

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

/**
 * This class runs all benchmarks
 */
public final class BenchmarkRunner {
    public static void main(String[] args) {
        OptionsBuilder options = new OptionsBuilder();
        options.include(parseArgs(args));
        try {
            new Runner(options.forks(1).build(), new CustomOutputFormat(System.out, VerboseMode.EXTRA)).run();
        } catch (RunnerException e) {
            e.printStackTrace();
        }
    }

    private static String parseArgs(String[] args) {
        String arg = args.length >= 1 ? args[0] : "";
        switch (arg) {
            case "-h":
            case "--help": {
                System.err.println("-h, --help                                 show help");
                System.err.println("-b [benchmark], --benchmark [benchmark]    run benchmark(class name required)");
                System.exit(0);
                break;
            }
            case "-b":
            case "--benchmark": {
                if(args.length < 2) {
                    System.err.println("Second argument(benchmark class) required!");
                    System.exit(0);
                }
                return args[2];
            }
            default:
                System.err.println("WTF! Argument not expected!");
                System.exit(1);
        }
        return "";
    }
}
