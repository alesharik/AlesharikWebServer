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
