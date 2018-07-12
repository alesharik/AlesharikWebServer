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
import com.alesharik.webserver.module.http.http.data.ContentType;
import com.alesharik.webserver.module.http.http.data.Cookie;
import com.alesharik.webserver.module.http.http.data.MimeType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@SuppressWarnings({"unchecked", "WeakerAccess"})
@NotThreadSafe
@ToString
public class Response implements Recyclable {
    private static final CachedObjectFactory<Response> factory = new SmartCachedObjectFactory<>(Response::new);

    @Getter
    protected HttpStatus status;
    @Setter
    @Getter
    protected HttpVersion version = HttpVersion.HTTP_1_1;
    @Getter
    protected List<String> headers = new ArrayList<>();

    protected OutputBuffer buffer;
    protected EncodedWriter writer;

    protected final Set<Cookie> cookies = new CopyOnWriteArraySet<>();
    @Getter
    protected long creationTime = System.currentTimeMillis();
    @Getter
    protected String upgrade;

    /**
     * 0 - is Content-Length header set
     * 1 - Upgrade
     */
    protected final BitSet marks = new BitSet(8);

    protected Response() {
        buffer = new OutputBuffer();
        writer = new EncodedWriter(buffer, StandardCharsets.ISO_8859_1);
        status = HttpStatus.NOT_IMPLEMENTED_501;
    }

    public static Response getResponse() {
        Response instance = factory.getInstance();
        instance.buffer = new OutputBuffer();
        instance.writer = new EncodedWriter(instance.buffer, StandardCharsets.ISO_8859_1);
        instance.status = HttpStatus.NOT_IMPLEMENTED_501;
        instance.creationTime = System.currentTimeMillis();
        return instance;
    }

    public static void delete(Response response) {
        factory.putInstance(response);
    }

    public void respond(HttpStatus status) {
        this.status = status;
    }

    public <T> void addHeader(Header<T> header, T value) {
        headers.add(header.build(value));
    }

    public void addHeader(String header, Object value) {
        addHeader(HeaderManager.getHeaderByName(header), value);
    }

    public OutputBuffer getOutputBuffer() {
        return buffer;
    }

    public EncodedWriter getWriter() {
        return writer;
    }

    public void upgrade(String protocol) {
        this.upgrade = protocol;
        this.marks.set(1, true);
    }

    public boolean isUpgraded() {
        return marks.get(1);
    }

    //====================Builders====================\\

    public String toStringResponse() {
        buildUtilsHeaders();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(version.getValue());
        stringBuilder.append(' ');
        stringBuilder.append(status.getCode());
        stringBuilder.append(' ');
        stringBuilder.append(status.getStatus());
        stringBuilder.append("\r\n");
        for(String header : headers) {
            stringBuilder.append(header);
            stringBuilder.append("\r\n");
        }
        stringBuilder.append("\r\n");
        stringBuilder.append(new String(buffer.toByteArray(), writer.charset));
        return stringBuilder.toString();
    }

    public byte[] toByteArray() {
        buildUtilsHeaders();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(version.getValue());
        stringBuilder.append(' ');
        stringBuilder.append(status.getCode());
        stringBuilder.append(' ');
        stringBuilder.append(status.getStatus());
        stringBuilder.append("\r\n");
        for(String header : headers) {
            stringBuilder.append(header);
            stringBuilder.append("\r\n");
        }
        stringBuilder.append("\r\n");
        byte[] header = stringBuilder.toString().getBytes(StandardCharsets.ISO_8859_1);
        byte[] bytes = buffer.toByteArray();

        byte[] ret = new byte[header.length + bytes.length];
        System.arraycopy(header, 0, ret, 0, header.length);
        System.arraycopy(bytes, 0, ret, header.length, bytes.length);
        return ret;
    }

    private void buildUtilsHeaders() {
        if(cookies.size() > 0) {
            for(Cookie cookie : cookies) {
                addHeader(HeaderManager.getHeaderByName("Set-Cookie"), cookie);
            }
        }
        if(!marks.get(0)) {
            setContentLength(buffer.toByteArray().length);
        }
    }

    //====================Utilities====================\\

    public void setType(MimeType type, Charset charset) {
        writer.setCharset(charset);
        addHeader(HeaderManager.getHeaderByName("Content-Type"), new ContentType(type, charset));
    }

    public void redirect(String location) {
        respond(HttpStatus.FOUND_302);
        try {
            addHeader(HeaderManager.getHeaderByName("Location"), new URI(location));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void setContentLength(long length) {
        addHeader(HeaderManager.getHeaderByName("Content-Length"), length);
        marks.set(0, true);
    }

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public Cookie[] getCookies() {
        return cookies.toArray(new Cookie[0]);
    }

    public int getResponseCode() {
        return status.getCode();
    }

    public byte[] getBody() {
        return buffer.toByteArray();
    }

    public void clearBody() {
        buffer.clear();
    }

    @Nonnull
    public Charset getBodyCharset() {
        return writer.charset;
    }

    @Override
    public void recycle() {
        version = HttpVersion.HTTP_1_1;
        headers.clear();
        cookies.clear();
        buffer.clear();
        writer.setCharset(StandardCharsets.ISO_8859_1);
        status = HttpStatus.NOT_IMPLEMENTED_501;
        creationTime = -1;
        marks.clear();
    }
}
