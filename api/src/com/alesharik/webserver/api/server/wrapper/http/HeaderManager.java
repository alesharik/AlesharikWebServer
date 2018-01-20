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
import com.alesharik.webserver.api.server.wrapper.http.data.ContentRange;
import com.alesharik.webserver.api.server.wrapper.http.data.ContentType;
import com.alesharik.webserver.api.server.wrapper.http.data.ETag;
import com.alesharik.webserver.api.server.wrapper.http.data.Encoding;
import com.alesharik.webserver.api.server.wrapper.http.data.Host;
import com.alesharik.webserver.api.server.wrapper.http.data.Range;
import com.alesharik.webserver.api.server.wrapper.http.data.ReferrerPolicy;
import com.alesharik.webserver.api.server.wrapper.http.data.TkType;
import com.alesharik.webserver.api.server.wrapper.http.data.UserAgent;
import com.alesharik.webserver.api.server.wrapper.http.data.Warning;
import com.alesharik.webserver.api.server.wrapper.http.header.AcceptCharsetHeader;
import com.alesharik.webserver.api.server.wrapper.http.header.AcceptEncodingHeader;
import com.alesharik.webserver.api.server.wrapper.http.header.AcceptHeader;
import com.alesharik.webserver.api.server.wrapper.http.header.AcceptLanguageHeader;
import com.alesharik.webserver.api.server.wrapper.http.header.AcceptRangesHeader;
import com.alesharik.webserver.api.server.wrapper.http.header.AuthenticateHeader;
import com.alesharik.webserver.api.server.wrapper.http.header.AuthorizationHeader;
import com.alesharik.webserver.api.server.wrapper.http.header.ConstantHeader;
import com.alesharik.webserver.api.server.wrapper.http.header.CookieHeader;
import com.alesharik.webserver.api.server.wrapper.http.header.IfETagHeader;
import com.alesharik.webserver.api.server.wrapper.http.header.IntHeader;
import com.alesharik.webserver.api.server.wrapper.http.header.ListHeader;
import com.alesharik.webserver.api.server.wrapper.http.header.ObjectHeader;
import com.alesharik.webserver.api.server.wrapper.http.header.SetCookieHeader;
import com.alesharik.webserver.api.server.wrapper.http.header.StringHeader;
import lombok.experimental.UtilityClass;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * <table BORDER=1 CELLPADDING=3 CELLSPACING=1>
 * <thead>
 * <tr>
 * <td>Name</td>
 * <td>Type</td>
 * <td>Example</td>
 * <td>Description(optional)</td>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td colspan="5" scope="colgroup"><center>Authentication</center></td>
 * </tr>
 * <tr>
 * <td>WWW-Authenticate</td>
 * <td>{@link com.alesharik.webserver.api.server.wrapper.http.data.Authentication}</td>
 * <td>WWW-Authenticate: Basic realm="Access to the staging site"</td>
 * </tr>
 * <tr>
 * <td>Authorization</td>
 * <td>{@link com.alesharik.webserver.api.server.wrapper.http.data.Authorization}</td>
 * <td>Authorization: Basic YWxhZGRpbjpvcGVuc2VzYW1l</td>
 * </tr>
 * <tr>
 * <td>Proxy-Authenticate</td>
 * <td>{@link com.alesharik.webserver.api.server.wrapper.http.data.Authentication}</td>
 * <td>WWW-Proxy-Authenticate: Basic realm="Access to the staging site"</td>
 * </tr>
 * <tr>
 * <td>Proxy-Authorization</td>
 * <td>{@link com.alesharik.webserver.api.server.wrapper.http.data.Authorization}</td>
 * <td>Proxy-Authorization: Basic YWxhZGRpbjpvcGVuc2VzYW1l</td>
 * </tr>
 * <tr>
 * <td colspan="5" scope="colgroup"><center>Caching</center></td>
 * </tr>
 * <tr>
 * <td>Age</td>
 * <td>{@link Integer}</td>
 * <td>Age: 24</td>
 * </tr>
 * <tr>
 * <td>Expires</td>
 * <td>{@link Date}</td>
 * <td>Expires: Wed, 21 Oct 2015 07:28:00 GMT</td>
 * </tr>
 * <tr>
 * <td>Cache-Control</td>
 * <td>NOT SUPPORTED</td>
 * <td>NOT SUPPORTED</td>
 * </tr>
 * <tr>
 * <td>Pragma</td>
 * <td>{@link Void}</td>
 * <td>Pragma: no-cache</td>
 * <td>Support only no-cache value</td>
 * </tr>
 * <tr>
 * <td>Warning</td>
 * <td>{@link Warning}</td>
 * <td>Warning: 112 - "cache down" "Wed, 21 Oct 2015 07:28:00 GMT"</td>
 * </tr>
 * <tr>
 * <td colspan="5" scope="colgroup"><center>Conditionals</center></td>
 * </tr>
 * <tr>
 * <td>Last-Modified</td>
 * <td>{@link Date}</td>
 * <td>Last-Modified: Wed, 21 Oct 2015 07:28:00 GMT</td>
 * </tr>
 * <tr>
 * <td>ETag</td>
 * <td>{@link ETag}</td>
 * <td>ETag: W/"0815"</td>
 * </tr>
 * <tr>
 * <td>If-Match</td>
 * <td>{@link ETag}[]</td>
 * <td>If-Match: W/"67ab43", "54ed21", "7892dd"</td>
 * </tr>
 * <tr>
 * <td>If-None-Match</td>
 * <td>{@link ETag}[]</td>
 * <td>If-None-Match: W/"67ab43", "54ed21", "7892dd"</td>
 * </tr>
 * <tr>
 * <td>If-Modified-Since</td>
 * <td>{@link Date}</td>
 * <td>If-Modified-Since: Wed, 21 Oct 2015 07:28:00 GMT</td>
 * </tr>
 * <tr>
 * <td>If-Unmodified-Since</td>
 * <td>{@link Date}</td>
 * <td>If-Unmodified-Since: Wed, 21 Oct 2015 07:28:00 GMT</td>
 * </tr>
 * <tr>
 * <td colspan="5" scope="colgroup"><center>Connection management</center></td>
 * </tr>
 * <tr>
 * <td>Connection</td>
 * <td>String[]</td>
 * <td>Connection: Upgrade</td>
 * </tr>
 * <tr>
 * <td colspan="5" scope="colgroup"><center>Content negotiation</center></td>
 * </tr>
 * <tr>
 * <td>Accept</td>
 * <td>mime types({@link com.alesharik.webserver.api.server.wrapper.http.data.WeightMimeType}[])</td>
 * <td>Accept: text/plain, application/xml;q=0.5, *;1=0.1</td>
 * </tr>
 * <tr>
 * <td>Accept-Charset</td>
 * <td>{@link com.alesharik.webserver.api.server.wrapper.http.data.WeightCharset}[]</td>
 * <td>Accept-Charset: utf-8, iso-8859-1;q=0.5, *;q=0.1</td>
 * </tr>
 * <tr>
 * <td>Accept-Encoding</td>
 * <td>{@link com.alesharik.webserver.api.server.wrapper.http.data.WeightEncoding}[]</td>
 * <td>Accept-Encoding: br, gzip;q=0.8, *;q=0.1</td>
 * </tr>
 * <tr>
 * <td>Accept-Language</td>
 * <td>{@link com.alesharik.webserver.api.server.wrapper.http.data.WeightLocale}[]</td>
 * <td>Accept-Language: fr-CH, en;q=0.9, *;q=0.5</td>
 * </tr>
 * <tr>
 * <td colspan="5" scope="colgroup"><center>Controls</center></td>
 * </tr>
 * <tr>
 * <td>Expect</td>
 * <td>{@link Void}</td>
 * <td>Expect: 100-continue</td>
 * <td>The only expectation defined in the specification is Expect: 100-continue. No other values supported</td>
 * </tr>
 * <tr>
 * <td colspan="5" scope="colgroup"><center>Do Not Track</center></td>
 * </tr>
 * <tr>
 * <td>DNT</td>
 * <td>{@link Integer}</td>
 * <td>DNT: 1</td>
 * <td>1 - on, 0 - off</td>
 * </tr>
 * <tr>
 * <td>Tk</td>
 * <td>{@link TkType}</td>
 * <td>Tk: !</td>
 * </tr>
 * <tr>
 * <td colspan="5" scope="colgroup"><center>Message body information</center></td>
 * </tr>
 * <tr>
 * <td>Content-Length</td>
 * <td>{@link Long}</td>
 * <td>Content-Length: 1234</td>
 * </tr>
 * <tr>
 * <td>Content-Type</td>
 * <td>{@link ContentType}</td>
 * <td>Content-Type: text/html; charset=utf-8</td>
 * </tr>
 * <tr>
 * <td>Content-Encoding</td>
 * <td>{@link Encoding}</td>
 * <td>Content-Encoding: identity</td>
 * <td>{@link Encoding#ALL} not allowed!</td>
 * </tr>
 * <tr>
 * <td>Content-Language</td>
 * <td>{@link Locale}[]</td>
 * <td>Content-Language: de-DE, en-CA</td>
 * </tr>
 * <tr>
 * <td>Content-Location</td>
 * <td>{@link java.net.URI}</td>
 * <td>Content-Location: /index</td>
 * <td>URI MUST be relative!</td>
 * </tr>
 * <tr>
 * <td colspan="5" scope="colgroup"><center>Response context</center></td>
 * </tr>
 * <tr>
 * <td>Allow</td>
 * <td>{@link Method}[]</td>
 * <td>Allow: GET, POST</td>
 * </tr>
 * <tr>
 * <td>Server</td>
 * <td>{@link String}</td>
 * <td>Server: Apache/1.0 (Unix)</td>
 * </tr>
 * <tr>
 * <td colspan="5" scope="colgroup"><center>Cookies</center></td>
 * </tr>
 * <tr>
 * <td>Cookie</td>
 * <td>{@link com.alesharik.webserver.api.server.wrapper.http.data.Cookie}[]</td>
 * <td>Cookie: yummy_cookie=choco; tasty_cookie=strawberry</td>
 * </tr>
 * <tr>
 * <td>Set-Cookie</td>
 * <td>{@link com.alesharik.webserver.api.server.wrapper.http.data.Cookie}</td>
 * <td>Set-Cookie: yummy_cookie=choco</td>
 * </tr>
 * <tr>
 * <td colspan="5" scope="colgroup"><center>Request context</center></td>
 * </tr>
 * <tr>
 * <td>From</td>
 * <td>{@link String}</td>
 * <td>From: webmaster@gmail.com</td>
 * <td>String represents an email</td>
 * </tr>
 * <tr>
 * <td>Host</td>
 * <td>{@link Host}</td>
 * <td>Host: localhost:3456</td>
 * </tr>
 * <tr>
 * <td>Referer</td>
 * <td>{@link URI}</td>
 * <td>Referer: https://developer.mozilla.org/en-US/docs/Web/JavaScript</td>
 * </tr>
 * <tr>
 * <td>Referrer-Policy</td>
 * <td>{@link ReferrerPolicy}</td>
 * <td>Referrer-Policy: no-referrer</td>
 * </tr>
 * <tr>
 * <td>User-Agent</td>
 * <td>{@link UserAgent}</td>
 * <td>User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36</td>
 * </tr>
 * <tr>
 * <td colspan="5" scope="colgroup"><center>Redirects</center></td>
 * </tr>
 * <tr>
 * <td>Location</td>
 * <td>{@link URI}</td>
 * <td>Location: /index.html</td>
 * <td>URI must be relative!</td>
 * </tr>
 * <tr>
 * <td colspan="5" scope="colgroup"><center>Range</center></td>
 * </tr>
 * <tr>
 * <td>Accept-Ranges</td>
 * <td>{@link com.alesharik.webserver.api.server.wrapper.http.header.AcceptRangesHeader.RangeType}</td>
 * <td>Accept-Ranges: bytes</td>
 * </tr>
 * <tr>
 * <td>Content-Range</td>
 * <td>{@link ContentRange}</td>
 * <td>Content-Range: bytes 1-2/3</td>
 * </tr>
 * <tr>
 * <td>Range</td>
 * <td>{@link Range}[]</td>
 * <td>Range: bytes=200-1000, 2000-6576, 19000-</td>
 * </tr>
 * <tr>
 * <td colspan="5" scope="colgroup"><center>Other</center></td>
 * </tr>
 * <tr>
 * <td>Upgrade</td>
 * <td>String</td>
 * <td>Upgrade: websocket</td>
 * </tr>
 * <tr>
 * <td colspan="5" scope="colgroup"><center>WebSocket</center></td>
 * </tr>
 * <tr>
 * <td>Sec-WebSocket-Version</td>
 * <td>String</td>
 * <td>Sec-WebSocket-Version: 13</td>
 * <td>Provide requested WebSocket protocol version</td>
 * </tr>
 * <tr>
 * <td>Sec-WebSocket-Key</td>
 * <td>String</td>
 * <td>Sec-WebSocket-Key: Iv8io/9s+lYFgZWcXczP8Q==</td>
 * <td>Provide Base64 encoded WS key</td>
 * </tr>
 * <tr>
 * <td>Sec-WebSocket-Accept</td>
 * <td>String</td>
 * <td>Sec-WebSocket-Accept: hsBlbuDTkk24srzEOTBUlZAlC2g=</td>
 * <td>Allow browser to manage WS connections</td>
 * </tr>
 * </tbody>
 * </table>
 * DO not support AWS4-HMAC-SHA256 authorization header<br>
 * Cache-Control not supported because of compatibility
 */
@UtilityClass
public class HeaderManager {
    public static final ThreadLocal<SimpleDateFormat> WEB_DATE_FORMAT = ThreadLocal.withInitial(() -> {
        SimpleDateFormat format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format;
    });

    private static final Map<String, Header<?>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    static {
        //Authentication
        headers.put("WWW-Authenticate", new AuthenticateHeader("WWW-Authenticate"));
        headers.put("Authorization", new AuthorizationHeader("Authorization"));
        headers.put("Proxy-Authenticate", new AuthenticateHeader("Proxy-Authenticate"));
        headers.put("Proxy-Authorization", new AuthorizationHeader("Proxy-Authorization"));

        //Caching
        headers.put("Age", new IntHeader("Age"));
        headers.put("Expires", new ObjectHeader<>("Expires", new DateFactory()));
        headers.put("Pragma", new ConstantHeader("Pragma"));
        headers.put("Warning", new ObjectHeader<>("Warning", new WarningFactory()));

        //Conditionals
        headers.put("Last-Modified", new ObjectHeader<>("Last-Modified", new DateFactory()));
        headers.put("ETag", new ObjectHeader<>("ETag", new ETagFactory()));
        headers.put("If-Match", new IfETagHeader("If-Match"));
        headers.put("If-None-Match", new IfETagHeader("If-None-Match"));
        headers.put("If-Modified-Since", new ObjectHeader<>("If-Modified-Since", new DateFactory()));
        headers.put("If-Unmodified-Since", new ObjectHeader<>("If-Unmodified-Since", new DateFactory()));

        //TODO Connection management
        headers.put("Connection", new ListHeader<>("Connection", new StringFactory()));

        //Content negotiation
        headers.put("Accept", new AcceptHeader());
        headers.put("Accept-Charset", new AcceptCharsetHeader());
        headers.put("Accept-Encoding", new AcceptEncodingHeader());
        headers.put("Accept-Language", new AcceptLanguageHeader());

        //Controls
        headers.put("Expect", new ConstantHeader("Expect"));

        headers.put("Cookie", new CookieHeader());
        headers.put("Set-Cookie", new SetCookieHeader());
        //TODO cors

        //Do Not Track
        headers.put("DNT", new IntHeader("DNT"));
        headers.put("Tk", new ObjectHeader<>("Tk", new TkFactory()));

        //TODO Downloads

        //Message body information
        headers.put("Content-Length", new ObjectHeader<>("Content-Length", new LongFactory()));
        headers.put("Content-Type", new ObjectHeader<>("Content-Type", new ContentTypeFactory()));
        headers.put("Content-Encoding", new ObjectHeader<>("Content-Encoding", new EncodingFactory()));
        headers.put("Content-Location", new ObjectHeader<>("Content-Location", new UriFactory()));
        headers.put("Content-Language", new ListHeader<>("Content-Language", new LocaleFactory()));

        //Request context
        headers.put("From", new StringHeader("From"));//Store email address as string
        headers.put("Host", new ObjectHeader<>("Host", new HostFactory()));
        headers.put("Referer", new ObjectHeader<>("Referer", new UriFactory()));
        headers.put("Referrer-Policy", new ObjectHeader<>("Referrer-Policy", new ReferrerPolicyFactory()));
        headers.put("User-Agent", new ObjectHeader<>("User-Agent", new UserAgentFactory()));

        //Response context
        headers.put("Allow", new ListHeader<>("Allow", new Method.Factory()));
        headers.put("Server", new StringHeader("Server"));

        //Redirects
        headers.put("Location", new ObjectHeader<>("Location", new UriFactory()));

        headers.put("Accept-Ranges", new AcceptRangesHeader());
        headers.put("Content-Range", new ObjectHeader<>("Content-Range", new ContentRangeFactory()));
        headers.put("Range", new ObjectHeader<>("Range", new RangeFactory()));

        //Other
        headers.put("Upgrade", new StringHeader("Upgrade"));

        //WebSocket
        headers.put("Sec-WebSocket-Version", new IntHeader("Sec-WebSocket-Version"));
        headers.put("Sec-WebSocket-Key", new StringHeader("Sec-WebSocket-Key"));
        headers.put("Sec-WebSocket-Accept", new StringHeader("Sec-WebSocket-Accept"));
    }

    public static <T extends Header> T getHeaderByName(String name, Class<T> clazz) {
        return clazz.cast(headers.get(name));
    }

    private static final class HostFactory implements ObjectHeader.Factory<Host> {

        @Override
        public Host newInstance(String s) {
            int doubledotIndex = s.indexOf(':');
            if(doubledotIndex == -1)
                return new Host(s);
            else {
                String[] divided = Utils.divideStringUnsafe(s, doubledotIndex, 1);
                return new Host(divided[0], Integer.parseInt(divided[1]));
            }
        }

        @Override
        public String toString(Host host) {
            return host.getHost() + (host.hasPort() ? ":" + host.getPort() : "");
        }
    }

    private static final class UriFactory implements ObjectHeader.Factory<URI> {

        @Override
        public URI newInstance(String s) {
            return URI.create(s);
        }

        @Override
        public String toString(URI url) {
            return url.toString();
        }
    }

    private static final class ReferrerPolicyFactory implements ObjectHeader.Factory<ReferrerPolicy> {

        @Override
        public ReferrerPolicy newInstance(String s) {
            return ReferrerPolicy.parseString(s);
        }

        @Override
        public String toString(ReferrerPolicy referrerPolicy) {
            return referrerPolicy.getName();
        }
    }

    private static final class UserAgentFactory implements ObjectHeader.Factory<UserAgent> {

        @Override
        public UserAgent newInstance(String s) {
            String[] parts1 = Utils.divideStringUnsafe(s, s.indexOf('/'), 1);
            String[] parts2 = Utils.divideStringUnsafe(parts1[1], parts1[1].indexOf(' '), 1);
            return new UserAgent(parts1[0], parts2[0], parts2[1]);
        }

        @Override
        public String toString(UserAgent userAgent) {
            return userAgent.getProduct() + '/' + userAgent.getVersion() + ' ' + userAgent.getComment();
        }
    }

    private static final class DateFactory implements ObjectHeader.Factory<Date> {

        @Override
        public Date newInstance(String s) {
            try {
                return WEB_DATE_FORMAT.get().parse(s);
            } catch (ParseException e) {
                return null;
            }
        }

        @Override
        public String toString(Date date) {
            return WEB_DATE_FORMAT.get().format(date);
        }
    }

    private static final class WarningFactory implements ObjectHeader.Factory<Warning> {

        @Override
        public Warning newInstance(String s) {
            return Warning.parse(s);
        }

        @Override
        public String toString(Warning warning) {
            return warning.toHeaderString();
        }
    }

    private static final class ETagFactory implements ObjectHeader.Factory<ETag> {

        @Override
        public ETag newInstance(String s) {
            return ETag.parse(s);
        }

        @Override
        public String toString(ETag eTag) {
            return eTag.toHeaderString();
        }
    }

    private static final class TkFactory implements ObjectHeader.Factory<TkType> {

        @Override
        public TkType newInstance(String s) {
            return TkType.parseType(s.charAt(0));
        }

        @Override
        public String toString(TkType tkType) {
            return String.valueOf(tkType.getSymbol());
        }
    }

    private static final class LongFactory implements ObjectHeader.Factory<Number> {

        @Override
        public Number newInstance(String s) {
            return Long.parseLong(s);
        }

        @Override
        public String toString(Number aLong) {
            return aLong.toString();
        }
    }

    private static final class ContentTypeFactory implements ObjectHeader.Factory<ContentType> {

        @Override
        public ContentType newInstance(String s) {
            return ContentType.parse(s);
        }

        @Override
        public String toString(ContentType contentType) {
            return contentType.toHeaderString();
        }
    }

    private static final class EncodingFactory implements ObjectHeader.Factory<Encoding> {

        @Override
        public Encoding newInstance(String s) {
            return Encoding.parseEncoding(s);
        }

        @Override
        public String toString(Encoding encoding) {
            if(encoding == Encoding.ALL)
                throw new IllegalArgumentException();
            return encoding.toString();
        }
    }

    private static final class LocaleFactory implements ListHeader.Factory<Locale> {

        @Override
        public Locale newInstance(String value) {
            return Locale.forLanguageTag(value);
        }

        @Override
        public String toString(Locale locale) {
            return locale.toLanguageTag();
        }

        @Override
        public Locale[] newArray(int size) {
            return new Locale[size];
        }
    }

    private static final class StringFactory implements ListHeader.Factory<String> {
        @Override
        public String newInstance(String value) {
            return value;
        }

        @Override
        public String toString(String s) {
            return s;
        }

        @Override
        public String[] newArray(int size) {
            return new String[size];
        }
    }

    private static final class ContentRangeFactory implements ObjectHeader.Factory<ContentRange> {

        @Override
        public ContentRange newInstance(String s) {
            return ContentRange.fromHeaderString(s);
        }

        @Override
        public String toString(ContentRange range) {
            return range.toHeaderString();
        }
    }

    private static final class RangeFactory implements ObjectHeader.Factory<Range[]> {

        @Override
        public Range[] newInstance(String s) {
            String[] parts = s.split("=", 2);
            AcceptRangesHeader.RangeType rangeType = AcceptRangesHeader.RangeType.parseRange(parts[0]);
            String[] parts1 = parts[1].split(", ");
            Range[] ret = new Range[parts1.length];
            for(int i = 0; i < parts1.length; i++) {
                ret[i] = Range.parse(rangeType, parts1[i]);
            }
            return ret;
        }

        @Override
        public String toString(Range[] ranges) {
            StringBuilder stringBuilder = new StringBuilder(ranges[0].getRangeType().toString());
            stringBuilder.append('=');
            boolean notFirst = false;
            for(Range range : ranges) {
                if(notFirst)
                    stringBuilder.append(", ");
                else
                    notFirst = true;
                stringBuilder.append(range.toHeaderString());
            }
            return stringBuilder.toString();
        }
    }
}
