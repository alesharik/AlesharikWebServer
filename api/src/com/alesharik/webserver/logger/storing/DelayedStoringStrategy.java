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

package com.alesharik.webserver.logger.storing;


import com.alesharik.webserver.logger.Logger;
import org.glassfish.grizzly.utils.Charsets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Timer;
import java.util.TimerTask;

public final class DelayedStoringStrategy extends StoringStrategy {
    private final Timer timer = new Timer("DelayedStoringStrategyTimer");
    private TimerTask timerTask;
    private StringBuffer stringBuffer;

    public DelayedStoringStrategy(File file) {
        super(file);
        setDelay(1000);
    }

    public void setDelay(long delay) {
        if(timerTask != null) {
            timerTask.cancel();
        }
        timer.schedule((timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    String str = stringBuffer.toString();
                    Files.write(file.toPath(), str.getBytes(Charsets.UTF8_CHARSET), StandardOpenOption.APPEND);
                    stringBuffer.delete(0, str.length());
                } catch (IOException e) {
                    Logger.log(e);
                }
            }
        }), 0, delay);
    }

    @Override
    public void open() throws IOException {
        checkFile();
        stringBuffer = new StringBuffer();
    }

    @Override
    public void publish(String prefix, String message) {
        stringBuffer.append(prefix);
        stringBuffer.append(": ");
        stringBuffer.append(message);
    }

    @Override
    public void close() throws IOException {
        timerTask.run();
        timer.cancel();
    }
}
