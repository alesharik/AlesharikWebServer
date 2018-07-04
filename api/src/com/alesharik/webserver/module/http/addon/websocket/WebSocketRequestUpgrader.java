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

package com.alesharik.webserver.module.http.addon.websocket;

import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.module.http.bundle.processor.Handler;
import com.alesharik.webserver.module.http.http.HeaderManager;
import com.alesharik.webserver.module.http.http.HttpStatus;
import com.alesharik.webserver.module.http.http.Request;
import com.alesharik.webserver.module.http.http.Response;
import com.alesharik.webserver.module.http.http.header.IntHeader;
import com.alesharik.webserver.module.http.http.header.ListHeader;
import com.alesharik.webserver.module.http.http.header.StringHeader;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

//TODO Origin header check
@Prefixes({"[HTTP]", "[WebSocket]", "[WebSocketRequestUpgrader]"})
public final class WebSocketRequestUpgrader implements Handler {
    private static final String MAGIC_STRING = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    @SuppressWarnings("unchecked")
    private static final ListHeader<String> CONNECTION_HEADER = HeaderManager.getHeaderByName("Connection");
    private static final StringHeader UPGRADE_HEADER = HeaderManager.getHeaderByName("Upgrade");
    private static final IntHeader WS_VERSION_HEADER = HeaderManager.getHeaderByName("Sec-WebSocket-Version");
    private static final StringHeader WS_KEY_HEADER = HeaderManager.getHeaderByName("Sec-WebSocket-Key");
    private static final StringHeader WS_ACCEPT_HEADER = HeaderManager.getHeaderByName("Sec-WebSocket-Accept");
    private static final ThreadLocal<MessageDigest> SHA1 = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    });//TODO replace with Hasher
    private final int supportedVersion;

    public WebSocketRequestUpgrader() {
        this(13);
    }

    /**
     * @param supportedVersion current version - 13
     */
    public WebSocketRequestUpgrader(int supportedVersion) {
        this.supportedVersion = supportedVersion;
    }

    @Override
    public void handle(@Nonnull Request request, @Nonnull Response response) {
        int version = request.getHeader(WS_VERSION_HEADER);
        if(version < supportedVersion) {
            System.err.println("Client " + request.getRemote().toString() + " tries to connect to WS endpoint with old WS protocol version: " + version);
            response.respond(HttpStatus.BAD_REQUEST_400);
            return;
        }
        String key = request.getHeader(WS_KEY_HEADER);
        response.respond(HttpStatus.SWITCHING_PROTOCOLS_101);
        response.addHeader(CONNECTION_HEADER, new String[]{"Upgrade"});
        response.addHeader(UPGRADE_HEADER, "websocket");
        response.addHeader(WS_ACCEPT_HEADER, magic(key));
        response.upgrade("websocket");
    }

    private static String magic(String key) {
        String concatenated = key.concat(MAGIC_STRING);
        MessageDigest digest = SHA1.get();
        try {
            byte[] hash = digest.digest(concatenated.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } finally {
            digest.reset();
        }
    }
}
