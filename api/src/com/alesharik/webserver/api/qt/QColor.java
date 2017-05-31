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
