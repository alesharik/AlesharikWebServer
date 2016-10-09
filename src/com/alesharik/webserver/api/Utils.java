package com.alesharik.webserver.api;

import com.alesharik.webserver.logger.Logger;
import one.nio.util.ByteArrayBuilder;
import one.nio.util.Hex;
import org.glassfish.grizzly.http.Cookie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.zip.CRC32;

public class Utils {
    private static final Utils INSTANCE = new Utils();

    static {
        try {
//            System.loadLibrary("libalesharikwebserver.so");
            InputStream in = Utils.class.getResourceAsStream("/libalesharikwebserver.so");

            if(in != null) {
                ByteArrayBuilder libData = readStream(in);
                in.close();

                String tmpDir = System.getProperty("java.io.tmpdir", "/tmp");
                File dll = new File(tmpDir, "libalesharikwebserver." + crc32(libData) + ".so");
                if(!dll.exists()) {
                    try (OutputStream out = new FileOutputStream(dll)) {
                        out.write(libData.buffer(), 0, libData.length());
                        out.close();
                    } catch (IOException e) {
                        throw e;
                    }
                }

                String libraryPath = dll.getAbsolutePath();
                System.load(libraryPath);
            } else {
                Logger.log("Cannot find native IO library");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private native String getExternalIp0();

    private static ByteArrayBuilder readStream(InputStream in) throws IOException {
        byte[] buffer = new byte[64000];
        ByteArrayBuilder builder = new ByteArrayBuilder(buffer.length);
        for(int bytes; (bytes = in.read(buffer)) > 0; ) {
            builder.append(buffer, 0, bytes);
        }
        return builder;
    }

    private static String crc32(ByteArrayBuilder builder) {
        CRC32 crc32 = new CRC32();
        crc32.update(builder.buffer(), 0, builder.length());
        return Hex.toHex((int) crc32.getValue());
    }

    /**
     * This function return external ip of first work network(network, in with program can connect to google server).
     * You can get this ip by <code>ping www.google.com</code> in first line. If function can't receive response as long
     * of 30 seconds, it will return 127.0.0.1
     *
     * @return dot-splitted ip address
     */
    public static String getExternalIp() {
        return INSTANCE.getExternalIp0();
    }

    /**
     * Get cookie for name of list of cookies
     *
     * @return {@link Cookie} or <code>null</code>
     */
    public static Cookie getCookieForName(String name, Cookie[] cookies) {
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(name)) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * Check if string is null or empty
     *
     * @throws NullPointerException     if string is <code>null</code>
     * @throws IllegalArgumentException if string is empty
     */
    public static void requireNotNullOrEmpty(String string) {
        Objects.requireNonNull(string);
        if(string.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }
}
