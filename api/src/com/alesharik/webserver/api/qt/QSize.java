package com.alesharik.webserver.api.qt;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

public class QSize implements QSerializable {
    private final AtomicInteger width;
    private final AtomicInteger height;

    public QSize(int w, int h) {
        width = new AtomicInteger(w);
        height = new AtomicInteger(h);
    }

    public QSize() {
        width = new AtomicInteger();
        height = new AtomicInteger();
    }

    public void setWidth(int w) {
        width.set(w);
    }

    public void setHeight(int h) {
        height.set(h);
    }

    public int getWidth() {
        return width.get();
    }

    public int getHeight() {
        return height.get();
    }

    @Override
    public void write(@Nonnull QDataStream stream) {
        stream.writeInt(width.get());
        stream.writeInt(height.get());
    }

    @Override
    public void read(@Nonnull QDataStream stream) {
        width.set(stream.readInt());
        height.set(stream.readInt());
    }
}
