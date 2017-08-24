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

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Getter
public class HttpStatus {
    private final int code;
    private final String status;

    private HttpStatus(int code, String status) {
        this.code = code;
        this.status = status;
    }

    public static final HttpStatus CONTINUE_100 = new HttpStatus(100, "Continue");
    public static final HttpStatus SWITCHING_PROTOCOLS_101 = new HttpStatus(101, "Switching Protocols");
    public static final HttpStatus WEB_SOCKET_PROTOCOL_HANDSHAKE_101 = new HttpStatus(101, "Web Socket Protocol Handshake");
    public static final HttpStatus OK_200 = new HttpStatus(200, "OK");
    public static final HttpStatus CREATED_201 = new HttpStatus(201, "Created");
    public static final HttpStatus ACCEPTED_202 = new HttpStatus(202, "Accepted");
    public static final HttpStatus NON_AUTHORITATIVE_INFORMATION_203 = new HttpStatus(203, "Not-Authoritative Information");
    public static final HttpStatus NO_CONTENT_204 = new HttpStatus(204, "No Content");
    public static final HttpStatus RESET_CONTENT_205 = new HttpStatus(205, "Reset Content");
    public static final HttpStatus PARTIAL_CONTENT_206 = new HttpStatus(206, "Partial Content");
    public static final HttpStatus MULTIPLE_CHOICES_300 = new HttpStatus(300, "Multiple Choices");
    public static final HttpStatus MOVED_PERMANENTLY_301 = new HttpStatus(301, "Moved Permanently");
    public static final HttpStatus FOUND_302 = new HttpStatus(302, "Found");
    public static final HttpStatus SEE_OTHER_303 = new HttpStatus(303, "See Other");
    public static final HttpStatus NOT_MODIFIED_304 = new HttpStatus(304, "Not Modified");
    public static final HttpStatus USE_PROXY_305 = new HttpStatus(305, "Use Proxy");
    public static final HttpStatus TEMPORARY_REDIRECT_307 = new HttpStatus(307, "Temporary Redirect");
    public static final HttpStatus BAD_REQUEST_400 = new HttpStatus(400, "Bad Request");
    public static final HttpStatus UNAUTHORIZED_401 = new HttpStatus(401, "Unauthorized");
    public static final HttpStatus PAYMENT_REQUIRED_402 = new HttpStatus(402, "Payment Required");
    public static final HttpStatus FORBIDDEN_403 = new HttpStatus(403, "Forbidden");
    public static final HttpStatus NOT_FOUND_404 = new HttpStatus(404, "Not Found");
    public static final HttpStatus METHOD_NOT_ALLOWED_405 = new HttpStatus(405, "Method Not Allowed");
    public static final HttpStatus NOT_ACCEPTABLE_406 = new HttpStatus(406, "Not Acceptable");
    public static final HttpStatus PROXY_AUTHENTICATION_REQUIRED_407 = new HttpStatus(407, "Proxy Authentication Required");
    public static final HttpStatus REQUEST_TIMEOUT_408 = new HttpStatus(408, "Request Timeout");
    public static final HttpStatus CONFLICT_409 = new HttpStatus(409, "Conflict");
    public static final HttpStatus GONE_410 = new HttpStatus(410, "Gone");
    public static final HttpStatus LENGTH_REQUIRED_411 = new HttpStatus(411, "Length Required");
    public static final HttpStatus PRECONDITION_FAILED_412 = new HttpStatus(412, "Precondition Failed");
    public static final HttpStatus REQUEST_ENTITY_TOO_LARGE_413 = new HttpStatus(413, "Request Entity Too Large");
    public static final HttpStatus REQUEST_URI_TOO_LONG_414 = new HttpStatus(414, "Request-URI Too Long");
    public static final HttpStatus UNSUPPORTED_MEDIA_TYPE_415 = new HttpStatus(415, "Unsupported Media Type");
    public static final HttpStatus REQUEST_RANGE_NOT_SATISFIABLE_416 = new HttpStatus(416, "Request Range Not Satisfiable");
    public static final HttpStatus EXPECTATION_FAILED_417 = new HttpStatus(417, "Expectation Failed");
    public static final HttpStatus INTERNAL_SERVER_ERROR_500 = new HttpStatus(500, "Internal Server Error");
    public static final HttpStatus NOT_IMPLEMENTED_501 = new HttpStatus(501, "Not Implemented");
    public static final HttpStatus BAD_GATEWAY_502 = new HttpStatus(502, "Bad Gateway");
    public static final HttpStatus SERVICE_UNAVAILABLE_503 = new HttpStatus(503, "Service Unavailable");
    public static final HttpStatus GATEWAY_TIMEOUT_504 = new HttpStatus(504, "Gateway Timeout");
    public static final HttpStatus HTTP_VERSION_NOT_SUPPORTED_505 = new HttpStatus(505, "HTTP Version Not Supported");
    public static final HttpStatus TOO_MANY_REQUESTS_429 = new HttpStatus(429, "Too Many Requests");
}
