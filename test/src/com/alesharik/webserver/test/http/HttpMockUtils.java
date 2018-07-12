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

package com.alesharik.webserver.test.http;

import com.alesharik.webserver.module.http.http.HttpStatus;
import com.alesharik.webserver.module.http.http.HttpVersion;
import com.alesharik.webserver.module.http.http.Method;
import com.alesharik.webserver.module.http.http.Request;
import com.alesharik.webserver.module.http.http.Response;
import com.alesharik.webserver.module.http.http.data.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@UtilityClass
public class HttpMockUtils {
    public static RequestBuilder request() {
        return new RequestBuilder();
    }

    public static Response response() {
        return new Response() {

        };
    }

    public static Validator verify(Response response) {
        return new Validator(response);
    }

    public static class RequestBuilder {
        private InetSocketAddress remote;
        private InetAddress local;
        private boolean secure = false;
        private byte[] body;
        private final List<String> headers = new ArrayList<>();
        private Method method = Method.GET;
        private HttpVersion version = HttpVersion.HTTP_1_1;
        private String rawUri = "/";

        public RequestBuilder withRemote(InetSocketAddress address) {
            this.remote = address;
            return this;
        }

        public RequestBuilder withLocal(InetAddress local) {
            this.local = local;
            return this;
        }

        public RequestBuilder setSecure(boolean secure) {
            this.secure = secure;
            return this;
        }

        public RequestBuilder addHeader(String header) {
            this.headers.add(header);
            return this;
        }

        public RequestBuilder withMethod(Method method) {
            this.method = method;
            return this;
        }

        public RequestBuilder withVersion(HttpVersion version) {
            this.version = version;
            return this;
        }

        public RequestBuilder withRawUri(String uri) {
            this.rawUri = uri;
            return this;
        }

        public RequestBuilder withBody(byte[] body) {
            this.body = body;
            return this;
        }

        public RequestBuilder withBody(String body) {
            this.body = body.getBytes(StandardCharsets.ISO_8859_1);
            return this;
        }

        public Request build() {
            return new RequestImpl(remote, local, secure, body, headers, method, version, rawUri);
        }

        private static final class RequestImpl extends Request {
            public RequestImpl(InetSocketAddress remote, InetAddress local, boolean secure, byte[] body, List<String> headers, Method method, HttpVersion version, String rawUri) {
                this.remote = remote;
                this.local = local;
                this.secure = secure;
                this.body = body == null ? null : Arrays.copyOf(body, body.length);
                this.headers = headers.toArray(new String[0]);
                this.method = method;
                this.httpVersion = version;
                this.rawUri = rawUri;
            }
        }
    }

    @RequiredArgsConstructor
    public static class Validator {
        private final Response response;

        public Validator respond(HttpStatus status) {
            assertEquals("Status not equals! Status: " + status + ", response: " + response.toStringResponse(), response.getResponseCode(), status.getCode());
            return this;
        }

        public Validator body(byte[] body) {
            assertArrayEquals("Body not equals! Request body: " + Arrays.toString(body) + ", response: " + response.toStringResponse(), response.getBody(), body);
            return this;
        }

        public Validator version(HttpVersion version) {
            assertEquals("Body not equals! Version: " + version + ", response: " + response.toStringResponse(), response.getVersion(), version);
            return this;
        }

        public Validator header(String header) {
            for(String s : response.getHeaders()) {
                if(s.equals(header))
                    return this;
            }
            throw new AssertionError("Header '" + header + "' not found! Response: " + response.toStringResponse());
        }

        public Validator cookie(Cookie cookie) {
            for(Cookie cookie1 : response.getCookies()) {
                if(cookie1.equals(cookie))
                    return this;
            }
            throw new AssertionError("Cookie " + cookie + " not found! Response: " + response.toStringResponse());
        }
    }
}
