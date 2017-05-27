package com.alesharik.webserver.api.qt;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

public class QPoint implements QSerializable {
    private final AtomicInteger x;
    private final AtomicInteger y;

    public QPoint(int x, int y) {
        this.x = new AtomicInteger(x);
        this.y = new AtomicInteger(y);
    }

    public QPoint() {
        this.x = new AtomicInteger();
        this.y = new AtomicInteger();
    }

    public void setX(int x) {
        this.x.set(x);
    }

    public void setY(int y) {
        this.y.set(y);
    }

    public int getX() {
        return this.x.get();
    }

    public int getY() {
        return this.y.get();
    }

    @Override
    public void write(@Nonnull QDataStream stream) {
        stream.writeInt(x.get());
        stream.writeInt(y.get());
    }

    @Override
    public void read(@Nonnull QDataStream stream) {
        this.x.set(stream.readInt());
        this.y.set(stream.readInt());
    }
}
