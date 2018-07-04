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

package com.alesharik.webserver.module.http.http;

import com.alesharik.webserver.api.cache.object.CachedObjectFactory;
import com.alesharik.webserver.api.cache.object.Recyclable;
import com.alesharik.webserver.api.cache.object.SmartCachedObjectFactory;
import com.alesharik.webserver.module.http.http.data.ContentDisposition;
import com.alesharik.webserver.module.http.http.data.ContentType;
import com.alesharik.webserver.module.http.http.data.Cookie;
import com.alesharik.webserver.module.http.http.data.Encoding;
import com.alesharik.webserver.module.http.http.header.ObjectHeader;
import com.alesharik.webserver.module.http.http.helper.ContentEncodingHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("WeakerAccess")
@NotThreadSafe
@ToString
public class Request implements Recyclable, Cloneable {
    public static final int CACHE_BODY = 1;
    public static final int CACHE_STRING_BODY = 2;
    public static final int CACHE_MULTIPART = 4;
    public static final int CACHE_MULTIPART_STRING = 8;

    protected final Map<String, Object> data = new ConcurrentHashMap<>();
    //====================Parsed data====================\\
    protected final Map<String, String> parameters = new HashMap<>();
    protected final MultiValuedMap<Header, Object> headerMap = new ArrayListValuedHashMap<>();
    protected Cookie[] cookies = new Cookie[0];
    /**
     * This is URI without parameters. Null means that URI not parsed yet
     */
    protected String uri = null;
    protected String stringBody;
    protected byte[] processedBody;
    protected Map<ContentDisposition, byte[]> multipart;
    protected Map<ContentDisposition, String> stringMultipart;
    //====================Metadata====================\\
    @Getter
    protected InetSocketAddress remote = null;
    @Getter
    protected InetAddress local = null;
    @Getter
    protected boolean secure = false;
    //====================Raw data====================\\
    @Getter
    protected String rawUri = null;
    protected String[] headers = new String[0];
    @Getter
    protected Method method = null;
    @Getter
    protected HttpVersion httpVersion = null;
    protected byte[] body = new byte[0];

    //====================Cache control====================\\
    /**
     * 1 - processedBody
     * 2 - stringBody
     * 3 - multipart
     * 4 - stringMultipart
     */
    @Setter
    protected int cacheControl = 0b1010;

    //====================Misc====================\\

    @Override
    public Request clone() {
        Request clone = new Request();
        clone.parameters.putAll(parameters);
        clone.cookies = cookies != null ? Arrays.copyOf(cookies, cookies.length) : null;
        clone.method = method;
        clone.httpVersion = httpVersion;
        clone.body = body != null ? Arrays.copyOf(body, body.length) : null;
        clone.rawUri = rawUri;
        clone.remote = remote;
        clone.local = local;
        clone.secure = secure;
        clone.uri = uri;
        clone.headerMap.putAll(headerMap);
        clone.headers = headers;
        clone.data.putAll(data);
        clone.cacheControl = cacheControl;
        clone.stringBody = stringBody;
        clone.multipart = multipart == null ? null : new HashMap<>(multipart);
        clone.stringMultipart = stringMultipart == null ? null : new HashMap<>(stringMultipart);
        clone.processedBody = processedBody == null ? null : Arrays.copyOf(processedBody, processedBody.length);
        return clone;
    }

    @Override
    public void recycle() {
        remote = null;
        local = null;
        secure = false;
        data.clear();

        rawUri = null;
        headers = new String[0];
        method = null;
        httpVersion = null;
        body = new byte[0];

        cookies = new Cookie[0];
        parameters.clear();
        uri = null;
        headerMap.clear();

        stringBody = null;
        multipart = null;
        stringMultipart = null;
        processedBody = null;

        cacheControl = 0b101011;
    }

    //====================Body====================\\

    @Nonnull
    public String getBodyAsString() {
        String stringBody = this.stringBody;
        if(stringBody != null)
            return stringBody;
        byte[] body = getBody();
        ContentType type = getContentType();
        if(type != null && type.getCharset() != null)
            stringBody = new String(body, type.getCharset());
        else
            stringBody = new String(body, StandardCharsets.ISO_8859_1);

        if((cacheControl & CACHE_STRING_BODY) == CACHE_STRING_BODY)
            this.stringBody = stringBody;

        return stringBody;
    }

    /**
     * @return <code>null</code> - request body isn't a multipart
     * @throws IllegalStateException with IOException
     */
    @Nullable
    public Map<ContentDisposition, byte[]> getBodyAsMultipart() {
        ContentType contentType = getContentType();
        if(!contentType.hasBoundary())
            return null;
        if(multipart != null)
            return multipart;

        Map<ContentDisposition, byte[]> multipart = new HashMap<>();
        String boundary = "--" + contentType.getBoundary();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(getBody()), StandardCharsets.ISO_8859_1))) {
            ContentDisposition contentDisposition = null;
            ContentType type = null;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            boolean isBody = false;

            String line;
            while((line = reader.readLine()) != null) {
                if(line.equals(boundary)) {
                    if(contentDisposition != null) {
                        contentDisposition.setContentType(type);
                        multipart.put(contentDisposition, stream.toByteArray());
                        stream.reset();
                        isBody = false;
                    }
                } else if(isBody) {
                    stream.write(line.getBytes(StandardCharsets.ISO_8859_1));
                } else if(line.toLowerCase().startsWith("Content-Disposition".toLowerCase())) {
                    //noinspection unchecked
                    contentDisposition = ((ObjectHeader<ContentDisposition>) HeaderManager.getHeaderByName("Content-Disposition")).getValue(line);
                } else if(line.toLowerCase().startsWith("Content-Type".toLowerCase())) {
                    //noinspection unchecked
                    type = ((ObjectHeader<ContentType>) HeaderManager.getHeaderByName("Content-Type")).getValue(line);
                } else if(StringUtils.isBlank(line))
                    isBody = true;
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        if((cacheControl & CACHE_MULTIPART) == CACHE_MULTIPART)
            this.multipart = multipart;

        return multipart;
    }

    /**
     * @return <code>null</code> - request body isn't a multipart
     */
    @Nullable
    public Map<ContentDisposition, String> getBodyAsStringMultipart() {
        if(stringMultipart != null)
            return stringMultipart;

        Map<ContentDisposition, byte[]> bodyAsMultipart = getBodyAsMultipart();
        Map<ContentDisposition, String> stringMultipart = new HashMap<>();
        for(Map.Entry<ContentDisposition, byte[]> entry : bodyAsMultipart.entrySet()) {
            ContentType contentType = entry.getKey().getContentType();
            if(contentType != null && contentType.getCharset() != null)
                stringMultipart.put(entry.getKey(), new String(entry.getValue(), contentType.getCharset()));
            else
                stringMultipart.put(entry.getKey(), new String(entry.getValue(), StandardCharsets.ISO_8859_1));
        }
        if((cacheControl & CACHE_MULTIPART_STRING) == CACHE_MULTIPART_STRING)
            this.stringMultipart = stringMultipart;
        return stringMultipart;
    }

    @Nonnull
    public byte[] getRawBody() {
        return body;
    }

    @Nonnull
    public byte[] getBody() {
        byte[] processedBody = this.processedBody;
        if(processedBody != null)
            return processedBody;
        Encoding header = getHeader("Content-Encoding");
        if(header != null)
            processedBody = ContentEncodingHelper.decode(body, header);
        else
            processedBody = body;
        if((cacheControl & CACHE_BODY) == CACHE_BODY)
            this.processedBody = body;
        return processedBody;
    }

    //====================Headers====================\\

    public boolean containsHeader(@Nonnull String header) {
        for(String s : headers) {
            if(s.toLowerCase().startsWith(header.toLowerCase()))
                return true;
        }
        return false;
    }

    @Nullable
    public <T> T getHeader(@Nonnull String name) {
        return getHeader(HeaderManager.getHeaderByName(name));
    }

    @Nonnull
    public <T> List<T> getHeaders(@Nonnull String name) {
        return getHeaders(HeaderManager.getHeaderByName(name));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getHeader(@Nonnull Header<T> header) {
        if(headerMap.containsKey(header))
            return (T) headerMap.get(header).iterator().next();
        String headerName = header.name.toLowerCase();
        for(String s : headers)
            if(s.toLowerCase().startsWith(headerName))
                headerMap.put(header, header.getValue(s));

        return headerMap.containsKey(header) ? (T) headerMap.get(header).iterator().next() : null;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public <T> List<T> getHeaders(@Nonnull Header<T> header) {
        if(headerMap.containsKey(header)) {
            List<T> ret = new ArrayList<>();
            for(Object o : headerMap.get(header))
                ret.add((T) o);
            return ret;
        }
        String headerName = header.name.toLowerCase();
        for(String s : headers)
            if(s.toLowerCase().startsWith(headerName))
                headerMap.put(header, header.getValue(s));

        List<T> ret = new ArrayList<>();
        for(Object o : headerMap.get(header))
            ret.add((T) o);
        return ret;
    }

    //====================Header helpers====================\\

    public ContentType getContentType() {
        return getHeader("Content-Type");
    }

    @Nonnull
    public Cookie[] getCookies() {
        if(cookies == null) {
            if(containsHeader("Cookie: ")) {
                cookies = getHeader("Cookie");
                return cookies;
            } else {
                cookies = new Cookie[0];
                return cookies;
            }
        }
        return cookies;
    }

    //====================Parameters/Path====================\\

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
        URI uri = URI.create(rawUri);
        String path = uri.getPath();
        if(path.contains("?"))
            path = path.substring(0, path.indexOf('?'));
        this.uri = path;
        String query = uri.getQuery();
        if(query != null) {
            for(String s : query.split("&")) {
                String[] parts = s.split("=", 2);
                parameters.put(parts[0], parts.length > 1 ? parts[1] : "");
            }
        }
    }

    //====================Data storage====================\\

    public void setData(String key, Object value) {
        data.put(key, value);
    }

    public <T> T getData(String key) {
        //noinspection unchecked
        return (T) data.get(key);
    }

    public static final class Builder extends Request {
        private static final CachedObjectFactory<Builder> factory = new SmartCachedObjectFactory<>(Builder::new);

        private final List<String> buildHeaders = new ArrayList<>();

        private Builder() {
        }

        @Override
        public void recycle() {
            super.recycle();
            buildHeaders.clear();
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

        public Builder withHeader(String header) {
            buildHeaders.add(header);
            return this;
        }

        public Builder buildHeaders() {
            this.headers = buildHeaders.toArray(new String[0]);
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
