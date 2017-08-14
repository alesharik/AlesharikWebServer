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

package com.alesharik.webserver.api.server.wrapper.addon;

import com.alesharik.webserver.api.memory.impl.ByteOffHeapVector;
import com.alesharik.webserver.api.server.wrapper.http.Request;
import com.alesharik.webserver.api.server.wrapper.http.Response;

import java.nio.ByteBuffer;

public interface Addon<A extends AddonAPI> {
    Response respond(Request request);

    void handleBytes(ByteOffHeapVector vector, long address);

    void setSender(ByteSender byteSender);

    A getApi();

    void close();

    interface ByteSender {
        void send(ByteBuffer byteBuffer);

        void close();
    }
}
