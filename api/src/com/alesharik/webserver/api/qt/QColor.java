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

import lombok.AccessLevel;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.awt.color.ColorSpace;

public class QColor extends Color implements QSerializable {
    public QColor(int r, int g, int b) {
        super(r, g, b);
    }

    public QColor(int r, int g, int b, int a) {
        super(r, g, b, a);
    }

    public QColor(int rgb) {
        super(rgb);
    }

    public QColor(int rgba, boolean hasalpha) {
        super(rgba, hasalpha);
    }

    public QColor(float r, float g, float b) {
        super(r, g, b);
    }

    public QColor(float r, float g, float b, float a) {
        super(r, g, b, a);
    }

    public QColor(ColorSpace cspace, float[] components, float alpha) {
        super(cspace, components, alpha);
    }

    @Override
    public void write(@Nonnull QDataStream stream) {

    }

    @Override
    public void read(@Nonnull QDataStream stream) {

    }

    public enum Spec {
        INVALID(0),
        RGB(1),
        HSV(2),
        CMYK(3),
        HSL(4);

        @Getter(AccessLevel.PRIVATE)
        private int id;

        Spec(int id) {
            this.id = id;
        }
    }
}
