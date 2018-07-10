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

package com.alesharik.webserver.platform;

import com.alesharik.webserver.exception.error.UnexpectedBehaviorError;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@UtilityClass
public class Native {
    static {
        if(isOsSupported()) {
            loadLibrary();
            new Daemon().start();
        }
    }

    static void staticInit() {
    }

    private static void loadLibrary() {
        try {
            InputStream res = Native.class.getClassLoader().getResourceAsStream("libalesharikwebserver.so");
            File lib = File.createTempFile("libalesharikwebserver", ".so");
            lib.deleteOnExit();

            OutputStream out = new FileOutputStream(lib);

            byte[] buf = new byte[4096];
            int nRead;
            while((nRead = res.read(buf)) == 4096)
                out.write(buf);
            out.write(buf, 0, nRead);
            out.flush();
            out.close();
            res.close();

            System.load(lib.getAbsolutePath());
        } catch (IOException e) {
            throw new UnexpectedBehaviorError(e);
        }
    }

    public static boolean isOsSupported() {
        return SystemUtils.IS_OS_LINUX;
    }

    private static final class Daemon extends Thread {
        private long fsUpdateTime = System.currentTimeMillis();

        public Daemon() {
            super("NativeUpdater");
            setDaemon(true);
        }

        @Override
        public void run() {
            while(isAlive() && !isInterrupted()) {
                CoreUtils.update();
                MemoryUtils.update();
                SystemInfoUtils.update();
                if(System.currentTimeMillis() - fsUpdateTime >= 60000) {
                    try {
                        FileSystemUtils.update();
                    } catch (Error e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
