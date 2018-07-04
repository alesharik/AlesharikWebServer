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

package com.alesharik.webserver.module.http.util;

import com.alesharik.webserver.module.http.bundle.processor.HttpProcessor;
import com.alesharik.webserver.module.http.http.Header;
import com.alesharik.webserver.module.http.http.HeaderManager;
import com.alesharik.webserver.module.http.http.HttpStatus;
import com.alesharik.webserver.module.http.http.Method;
import com.alesharik.webserver.module.http.http.Request;
import com.alesharik.webserver.module.http.http.Response;
import com.alesharik.webserver.module.http.http.data.ContentRange;
import com.alesharik.webserver.module.http.http.data.ContentType;
import com.alesharik.webserver.module.http.http.data.MimeType;
import com.alesharik.webserver.module.http.http.data.Range;
import com.alesharik.webserver.module.http.http.header.AcceptRangesHeader;
import com.alesharik.webserver.module.http.http.header.ObjectHeader;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * This {@link HttpProcessor} wraps ranged request logic
 */
//TODO refactor
@SuppressWarnings("unchecked")
public class RangeFileHttpProvider implements HttpProcessor {
    protected static final String DEFAULT_MULTIPART_BOUNDARY = "3d6b6a416f9b5";
    protected static final ContentType DEFAULT_MULTIPART_CONTENT_TYPE = new ContentType(new MimeType("multipart/byteranges", DEFAULT_MULTIPART_BOUNDARY));
    protected static final Header<AcceptRangesHeader.RangeType> acceptRangesHeader = HeaderManager.getHeaderByName("Accept-Ranges");
    protected static final Header<ContentRange> contentRangeHeader = HeaderManager.getHeaderByName("Content-Range");
    protected static final Header<Range[]> rangeHeader = HeaderManager.getHeaderByName("Range");
    protected static final ObjectHeader<ContentType> contentTypeHeader = HeaderManager.getHeaderByName("Content-Type");

    protected final FileContentProvider provider;

    public RangeFileHttpProvider(FileContentProvider provider) {
        this.provider = provider;
    }

    @Override
    public void process(@Nonnull Request request, @Nonnull Response response) {
        if(!provider.hasFile(request)) {
            response.respond(HttpStatus.NOT_FOUND_404);
            return;
        }

        if(request.getMethod() == Method.HEAD) {
            response.respond(HttpStatus.OK_200);
            response.addHeader(acceptRangesHeader, AcceptRangesHeader.RangeType.BYTES);
            response.setContentLength(provider.getLength(request));
        } else if(request.getMethod() == Method.GET) {
            Range[] ranges = request.getHeader(rangeHeader);
            ContentType contentType = new ContentType(provider.getMimeType(request));
            if(ranges == null) {
                response.respond(HttpStatus.REQUEST_RANGE_NOT_SATISFIABLE_416);
            } else if(ranges.length == 1) {
                Range range = ranges[0];
                byte[] data = provider.getRangedData(request, range.getStart(), range.getEnd());
                if(data.length == 0) {
                    response.respond(HttpStatus.REQUEST_RANGE_NOT_SATISFIABLE_416);
                    return;
                }
                long size = provider.getLength(request);
                ContentRange contentRange = new ContentRange(range.getRangeType(), range.getStart(), range.getEnd(), size);
                response.respond(HttpStatus.PARTIAL_CONTENT_206);
                response.addHeader(contentRangeHeader, contentRange);
                response.setContentLength(data.length);
                response.addHeader(contentTypeHeader, contentType);
                response.getOutputBuffer().write(data);
            } else {
                long size = provider.getLength(request);
                response.addHeader(contentTypeHeader, DEFAULT_MULTIPART_CONTENT_TYPE);
                response.respond(HttpStatus.PARTIAL_CONTENT_206);
                Map<String, byte[]> resp = new HashMap<>();
                for(Range range : ranges) {
                    byte[] data = provider.getRangedData(request, range.getStart(), range.getEnd());
                    if(data.length == 0) {
                        response.respond(HttpStatus.REQUEST_RANGE_NOT_SATISFIABLE_416);
                        return;
                    }
                    ContentRange contentRange = new ContentRange(range.getRangeType(), range.getStart(), range.getEnd(), size);
                    String headers = contentRangeHeader.build(contentRange) + "\r\n" + contentTypeHeader.build(contentType) + "\r\n\r\n";
                    resp.put(headers, data);
                }
                Map.Entry<String, byte[]>[] entries = resp.entrySet().toArray(new Map.Entry[0]);
                for(int i = 0; i < entries.length; i++) {
                    response.getWriter().write("--" + DEFAULT_MULTIPART_BOUNDARY + ((i + 1 == entries.length) ? "--" : "") + "\r\n");
                    response.getWriter().write(entries[i].getKey());
                    response.getOutputBuffer().write(entries[i].getValue());
                }
            }
        } else
            response.respond(HttpStatus.METHOD_NOT_ALLOWED_405);
    }
}
