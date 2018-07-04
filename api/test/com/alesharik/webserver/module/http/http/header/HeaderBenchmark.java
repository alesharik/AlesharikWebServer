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

package com.alesharik.webserver.module.http.http.header;

import com.alesharik.webserver.benchmark.BenchmarkTest;
import com.alesharik.webserver.module.http.http.data.Authentication;
import com.alesharik.webserver.module.http.http.data.Authorization;
import com.alesharik.webserver.module.http.http.data.ETag;
import com.alesharik.webserver.module.http.http.data.Encoding;
import com.alesharik.webserver.module.http.http.data.WeightCharset;
import com.alesharik.webserver.module.http.http.data.WeightEncoding;
import com.alesharik.webserver.module.http.http.data.WeightLocale;
import com.alesharik.webserver.module.http.http.data.WeightMimeType;
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

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@BenchmarkTest("headerTest")
@Measurement(timeUnit = TimeUnit.NANOSECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class HeaderBenchmark {
    private static final String toParseAcceptCharset = "Accept-Charset: utf-8, iso-8859-1;q=0.5, *;q=0.1";
    private final WeightCharset[] fromBuildAcceptCharset = new WeightCharset[]{
            new WeightCharset(Charsets.UTF8_CHARSET),
            new WeightCharset(Charset.forName("ISO-8859-1"), 0.5F),
            WeightCharset.anyCharset(0.1F)
    };
    private final AcceptCharsetHeader acceptCharsetHeader = new AcceptCharsetHeader();

    private static final String toParseAccept = "Accept: text/html, application/xhtml+xml, application/xml;q=0.9, */*;q=0.8";
    private final WeightMimeType[] fromBuildAccept = new WeightMimeType[]{
            new WeightMimeType("text", "html"),
            new WeightMimeType("application", "xhtml+xml"),
            new WeightMimeType("application", "xml", 0.9F),
            new WeightMimeType("*", "*", 0.8F)
    };
    private final AcceptHeader acceptHeader = new AcceptHeader();

    private static final String toParseAcceptEncoding = "Accept-Encoding: br;q=1.0, gzip;q=0.8, *;q=0.1, test;q=0.05";
    private final WeightEncoding[] fromBuildAcceptEncoding = new WeightEncoding[]{
            new WeightEncoding(Encoding.BR),
            new WeightEncoding(Encoding.GZIP, 0.8F),
            new WeightEncoding(Encoding.ALL, 0.1F)
    };
    private final AcceptEncodingHeader acceptEncodingHeader = new AcceptEncodingHeader();

    private static final String toParseAcceptLanguage = "Accept-Language: fr-CH, en;q=0.9, *;q=0.5";
    private final WeightLocale[] fromBuildAcceptLanguage = new WeightLocale[]{
            new WeightLocale(new Locale("en", "US")),
            new WeightLocale(new Locale("fr"), 0.8F),
            new WeightLocale(WeightLocale.ANY_LOCALE, 0.1F)
    };
    private final AcceptLanguageHeader acceptLanguageHeader = new AcceptLanguageHeader();

    private static final String toParseAcceptRanges = "Accept-Ranges: bytes";
    private final AcceptRangesHeader acceptRangesHeader = new AcceptRangesHeader();

    private static final String toParseIntHeader = "Test: 1";
    private final Integer fromBuildIntHeader = 1;
    private final IntHeader intHeader = new IntHeader("Test");

    private static final String toParseListHeader = "ByteOrder: bigEndian, bigEndian, littleEndian, bigEndian";
    private final ByteOrder[] fromBuildListHeader = new ByteOrder[]{
            ByteOrder.BIG_ENDIAN,
            ByteOrder.LITTLE_ENDIAN,
            ByteOrder.BIG_ENDIAN,
            ByteOrder.LITTLE_ENDIAN
    };
    private final ListHeader<ByteOrder> listHeader = new ListHeader<>("ByteOrder", new ListHeader.Factory<ByteOrder>() {
        @Override
        public ByteOrder newInstance(String value) {
            return value.equals("bigEndian") ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        }

        @Override
        public String toString(ByteOrder byteOrder) {
            return byteOrder == ByteOrder.BIG_ENDIAN ? "bigEndian" : "littleEndian";
        }

        @Override
        public ByteOrder[] newArray(int size) {
            return new ByteOrder[size];
        }
    });

    private static final String toParseStringHeader = "Asd: test";
    private static final String fromBuildStringHeader = "test";
    private final StringHeader stringHeader = new StringHeader("Asd");


    private static final String toParseObjectHeader = "Sdf: big";
    private final ByteOrder fromBuildObjectHeader = ByteOrder.BIG_ENDIAN;
    private final ObjectHeader<ByteOrder> objectHeader = new ObjectHeader<>("Sdf", new ObjectHeader.Factory<ByteOrder>() {
        @Override
        public ByteOrder newInstance(String s) {
            return s.equals("big") ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        }

        @Override
        public String toString(ByteOrder byteOrder) {
            return byteOrder == ByteOrder.BIG_ENDIAN ? "big" : "little";
        }
    });

    private static final String toParseAuthenticateHeader = "WWW-Authenticate: Basic realm=\"Access to the staging site\"";
    private final Authentication fromBuildAuthenticateHeader = new Authentication(Authentication.Type.BASIC, "Access to the staging site");
    private final AuthenticateHeader authenticateHeader = new AuthenticateHeader("WWW-Authenticate");

    private static final String toParseAuthorizationHeader = "Authorization: Basic YWxhZGRpbjpvcGVuc2VzYW1l";
    private final Authorization fromBuildAuthorizationHeader = new Authorization(Authentication.Type.BASIC, "YWxhZGRpbjpvcGVuc2VzYW1l");
    private final AuthorizationHeader authorizationHeader = new AuthorizationHeader("Authorization");

    private static final String toParseIfETagHeader = "Tess: W/\"test\", \"asd\", *";
    private final ETag[] fromBuildIfETagHeader = new ETag[]{
            new ETag("test", true),
            new ETag("asd", false),
            ETag.ANY_TAG
    };
    private final IfETagHeader ifETagHeader = new IfETagHeader("Tess");

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("acceptCharsetHeaderTest")
    @GroupThreads(2)
    public WeightCharset[] acceptCharsetParseTest() {
        return acceptCharsetHeader.getValue(toParseAcceptCharset);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("acceptCharsetHeaderTest")
    @GroupThreads(2)
    public String acceptCharsetBuildTest() {
        return acceptCharsetHeader.build(fromBuildAcceptCharset);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("acceptHeaderTest")
    @GroupThreads(2)
    public WeightMimeType[] acceptParseTest() {
        return acceptHeader.getValue(toParseAccept);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("acceptHeaderTest")
    @GroupThreads(2)
    public String acceptBuildTest() {
        return acceptHeader.build(fromBuildAccept);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("acceptEncodingHeaderTest")
    @GroupThreads(2)
    public WeightEncoding[] acceptEncodingParseTest() {
        return acceptEncodingHeader.getValue(toParseAcceptEncoding);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("acceptEncodingHeaderTest")
    @GroupThreads(2)
    public String acceptEncodingBuildTest() {
        return acceptEncodingHeader.build(fromBuildAcceptEncoding);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("acceptLanguageHeaderTest")
    @GroupThreads(2)
    public WeightLocale[] acceptLanguageParseTest() {
        return acceptLanguageHeader.getValue(toParseAcceptLanguage);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("acceptLanguageHeaderTest")
    @GroupThreads(2)
    public String acceptLanguageBuildTest() {
        return acceptLanguageHeader.build(fromBuildAcceptLanguage);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("acceptRangesHeaderTest")
    @GroupThreads(2)
    public AcceptRangesHeader.RangeType acceptRangesParseTest() {
        return acceptRangesHeader.getValue(toParseAcceptRanges);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("acceptRangesHeaderTest")
    @GroupThreads(2)
    public String acceptRangesBuildTest() {
        return acceptRangesHeader.build(AcceptRangesHeader.RangeType.BYTES);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("intHeaderTest")
    @GroupThreads(2)
    public Integer intHeaderParseTest() {
        return intHeader.getValue(toParseIntHeader);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("intHeaderTest")
    @GroupThreads(2)
    public String intHeaderBuildTest() {
        return intHeader.build(fromBuildIntHeader);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("listHeaderTest")
    @GroupThreads(2)
    public ByteOrder[] listHeaderParseTest() {
        return listHeader.getValue(toParseListHeader);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("listHeaderTest")
    @GroupThreads(2)
    public String listHeaderBuildTest() {
        return listHeader.build(fromBuildListHeader);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("stringHeaderTest")
    @GroupThreads(2)
    public String stringHeaderParseTest() {
        return stringHeader.getValue(toParseStringHeader);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("stringHeaderTest")
    @GroupThreads(2)
    public String stringHeaderBuildTest() {
        return stringHeader.build(fromBuildStringHeader);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("objectHeaderTest")
    @GroupThreads(2)
    public ByteOrder objectHeaderParseTest() {
        return objectHeader.getValue(toParseObjectHeader);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("objectHeaderTest")
    @GroupThreads(2)
    public String objectHeaderBuildTest() {
        return objectHeader.build(fromBuildObjectHeader);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("authenticateHeader")
    @GroupThreads(2)
    public Authentication wwwAuthenticateHeaderParseTest() {
        return authenticateHeader.getValue(toParseAuthenticateHeader);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("authenticateHeader")
    @GroupThreads(2)
    public String wwwAuthenticateHeaderBuildTest() {
        return authenticateHeader.build(fromBuildAuthenticateHeader);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("authorizationHeader")
    @GroupThreads(2)
    public Authorization authorizationHeaderParseTest() {
        return authorizationHeader.getValue(toParseAuthorizationHeader);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("authorizationHeader")
    @GroupThreads(2)
    public String authorizationHeaderBuildTest() {
        return authorizationHeader.build(fromBuildAuthorizationHeader);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("ifETagHeader")
    @GroupThreads(2)
    public ETag[] ifETagHeaderParseTest() {
        return ifETagHeader.getValue(toParseIfETagHeader);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Group("ifETagHeader")
    @GroupThreads(2)
    public String ifETagHeaderBuildTest() {
        return ifETagHeader.build(fromBuildIfETagHeader);
    }
}