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

package com.alesharik.webserver.api.server.wrapper.http;

import com.alesharik.webserver.api.Utils;
import com.alesharik.webserver.api.cache.object.CachedObjectFactory;
import com.alesharik.webserver.api.cache.object.Recyclable;
import com.alesharik.webserver.api.cache.object.SmartCachedObjectFactory;
import com.alesharik.webserver.api.server.wrapper.http.data.Cookie;
import com.alesharik.webserver.api.server.wrapper.http.header.CookieHeader;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("WeakerAccess")
public class Request implements Recyclable {
    /**
     * URI parameters. Stored as Key:Value
     */
    protected final Map<String, String> parameters;
    /**
     * This is cache for headers
     */
    protected final Map<Header, List<Object>> headerMap;
    protected Cookie[] cookies;
    @Getter
    protected Method method;
    @Getter
    protected HttpVersion httpVersion;
    @Getter
    protected byte[] body;
    /**
     * This is URI without parameters. Null means that URI not parsed yet
     */
    protected String uri = null;
    /**
     * This is raw URI
     */
    protected String rawUri;
    /**
     * This is header strings
     */
    protected String[] headers;
    protected InetSocketAddress remote;
    protected InetAddress local;
    protected boolean secure;

    {
        headerMap = new ConcurrentHashMap<>();
        parameters = new ConcurrentHashMap<>();
    }

    public boolean containsHeader(@Nonnull String header) {
        for(String s : headers) {
            if(s.startsWith(header))
                return true;
        }
        return false;
    }

    public String getRawUri() {
        return rawUri;
    }

    /**
     * Return URI without parameters
     */
    public String getContextPath() {
        if(uri == null)
            parseUri();
        return uri;
    }

    public boolean hasParameter(String key) {
        if(uri == null)
            parseUri();
        return parameters.containsKey(key);
    }

    public String getParameter(String key) {
        if(uri == null)
            parseUri();
        return parameters.get(key);
    }

    public Map<String, String> getParameters() {
        if(uri == null)
            parseUri();
        return Collections.unmodifiableMap(parameters);
    }

    private void parseUri() {
        int questionIndex = rawUri.indexOf('?');
        if(questionIndex == -1)
            uri = rawUri;
        else {
            String[] strings = Utils.divideStringUnsafe(rawUri, questionIndex, 1);
            uri = strings[0];

            String params = strings[1];

            boolean isColon = false;
            StringBuilder temp = new StringBuilder();
            List<String> paramParts = new ArrayList<>();
            for(char c : params.toCharArray()) {
                if(c == '"') {
                    isColon = !isColon;
                } else if(c == '&') {
                    if(isColon)
                        temp.append(c);
                    else {
                        paramParts.add(temp.toString());
                        temp.delete(0, temp.length());
                    }
                } else {
                    temp.append(c);
                }
            }
            if(temp.length() > 0)
                paramParts.add(temp.toString());
            for(String paramPart : paramParts) {
                int index = paramPart.indexOf('=');
                if(index == -1) {
                    System.out.println("Strange behavior: { rawUri: " + rawUri + ", uri: " + uri + ", params: " + params + ", paramParts: " + paramParts + "}! Recursion start!");
                    parseUri();
                }
                String[] divided = Utils.divideStringUnsafe(paramPart, index, 1);
                parameters.put(divided[0], divided[1]);
            }
        }
    }

    @Nullable
    public <T> T getHeader(Header<T> header, Class<T> cast) {
        if(headerMap.containsKey(header))
            return cast.cast(headerMap.get(header).get(0));
        else {
            if(!containsHeader(header.name))
                return null;
            for(String s : headers) {
                List<T> ret = new ArrayList<>();
                if(s.startsWith(header.name)) {
                    T t = header.getValue(s);
                    ret.add(t);
                }
                if(!ret.isEmpty()) {
                    headerMap.put(header, Collections.unmodifiableList(ret));
                    return ret.get(0);
                }
            }
        }
        return null;
    }

    @Nonnull
    public <T> List<T> getHeaders(Header<T> header, Class<T> cast) {
        if(headerMap.containsKey(header)) {
            List<T> ret = new ArrayList<>();
            for(Object o : headerMap.get(header)) {
                ret.add(cast.cast(o));
            }
            return ret;
        } else {
            if(!containsHeader(header.name))
                return Collections.emptyList();
            for(String s : headers) {
                List<T> ret = new ArrayList<>();
                if(s.startsWith(header.name)) {
                    T t = header.getValue(s);
                    ret.add(t);
                }
                if(!ret.isEmpty()) {
                    headerMap.put(header, Collections.unmodifiableList(ret));
                    return ret;
                }
            }
        }
        return Collections.emptyList();
    }

    @Nonnull
    public Cookie[] getCookies() {
        if(cookies == null) {
            if(containsHeader("Cookie: ")) {
                cookies = getHeader(HeaderManager.getHeaderByName("Cookie", CookieHeader.class), Cookie[].class);
                return cookies;
            } else {
                cookies = new Cookie[0];
                return cookies;
            }
        }
        return cookies;
    }

    public InetAddress getLocal() {
        return local;
    }

    public InetSocketAddress getRemote() {
        return remote;
    }

    public boolean isSecure() {
        return secure;
    }

    @Override
    public void recycle() {
        cookies = new Cookie[0];
        method = null;
        httpVersion = null;
        body = new byte[0];
        parameters.clear();
        rawUri = null;
        remote = null;
        local = null;
        secure = false;
        uri = null;
        headers = new String[0];
        headerMap.clear();
    }

    public static final class Builder extends Request {
        private static final CachedObjectFactory<Builder> factory = new SmartCachedObjectFactory<>(Builder::new);

        private Builder() {
        }

        public static Builder start(String firstLine) {
            return factory.getInstance().parse(firstLine);
        }

        public static void delete(Builder builder) {
            factory.putInstance(builder);
        }

        private Builder parse(String firstLine) {
            String[] startLine = firstLine.replaceAll("\u0000", "").split(" ");
            this.method = Method.valueOf(startLine[0]);
            if(startLine[1].contains("://")) {
                this.rawUri = startLine[1].replaceFirst(".*://.[^/]+", "");
            } else {
                this.rawUri = startLine[1];
            }
            this.httpVersion = HttpVersion.getValue(startLine[2]);
            return this;
        }

        public Builder withHeaders(String headers) {
            this.headers = headers.split("\r\n");
            return this;
        }

        public Builder withBody(byte[] body) {
            this.body = body;
            return this;
        }

        public Builder withInfo(InetSocketAddress remote, InetAddress local, boolean secure) {
            this.remote = remote;
            this.local = local;
            this.secure = secure;
            return this;
        }
    }
}
