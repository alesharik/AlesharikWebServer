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

package com.alesharik.webserver.module.http.http.helper;

import com.alesharik.webserver.module.http.http.data.Encoding;
import lombok.experimental.UtilityClass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

@UtilityClass
public class ContentEncodingHelper {
    private static final Encoding[] SUPPORTED = new Encoding[]{Encoding.IDENTITY, Encoding.GZIP, Encoding.DEFLATE};
    private static final ThreadLocal<Inflater> INFLATER = ThreadLocal.withInitial(Inflater::new);
    private static final ThreadLocal<Deflater> DEFLATER = ThreadLocal.withInitial(Deflater::new);

    public static byte[] decode(byte[] raw, Encoding encoding) {
        if(encoding == Encoding.GZIP)
            return decodeGzip(raw);
        else if(encoding == Encoding.DEFLATE)
            return decodeDeflate(raw);
        else if(encoding == Encoding.IDENTITY)
            return raw;
        else
            throw new IllegalArgumentException("Encoding not supported!");
    }

    private static byte[] decodeGzip(byte[] raw) {
        try (GZIPInputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(raw));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int nRead;
            while((nRead = inputStream.read(buf)) == 4096)
                out.write(buf);
            out.write(buf, 0, nRead);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] decodeDeflate(byte[] raw) {
        Inflater inflater = INFLATER.get();
        inflater.reset();
        inflater.setInput(raw);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int nRead;
            while((nRead = inflater.inflate(buf)) == 4096)
                out.write(buf);
            out.write(buf, 0, nRead);
            inflater.reset();
            return out.toByteArray();
        } catch (IOException | DataFormatException e) {
            throw new IllegalStateException(e);
        }
    }

    public static byte[] encode(byte[] raw, Encoding encoding) {
        if(encoding == Encoding.GZIP)
            return encodeGzip(raw);
        else if(encoding == Encoding.DEFLATE)
            return encodeDeflate(raw);
        else if(encoding == Encoding.IDENTITY)
            return raw;
        else
            throw new IllegalArgumentException("Encoding not supported!");
    }

    private static byte[] encodeDeflate(byte[] raw) {
        Deflater deflater = DEFLATER.get();
        deflater.reset();
        deflater.setInput(raw);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int nRead;
            while((nRead = deflater.deflate(buf)) == 4096)
                out.write(buf);
            out.write(buf, 0, nRead);
            deflater.reset();
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] encodeGzip(byte[] raw) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPOutputStream stream = new GZIPOutputStream(out)) {
            stream.write(raw);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Encoding[] getSupportedEncodings() {
        return SUPPORTED;
    }
}
