package com.alesharik.webserver.logger.storingStrategies;


import com.alesharik.webserver.logger.Logger;
import org.glassfish.grizzly.utils.Charsets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Timer;
import java.util.TimerTask;

public class DelayedStoringStrategy extends StoringStrategy {
    private final Timer timer = new Timer("DelayedStoringStrategyTimer");
    private TimerTask timerTask;
    private StringBuffer stringBuffer;

    protected DelayedStoringStrategy(File file) {
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
