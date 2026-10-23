// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

import java.util.Collection;

/** An axis-aligned rectangle given by its min and max corners. */
public record Bounds(double minX, double minY, double maxX, double maxY) {

    public Bounds {
        if (minX > maxX || minY > maxY) {
            throw new IllegalArgumentException("min corner must not exceed max corner");
        }
    }

    /** The rectangle spanned by two arbitrary corners, in any order. */
    public static Bounds spanning(Vec2 a, Vec2 b) {
        return new Bounds(Math.min(a.x(), b.x()), Math.min(a.y(), b.y()),
                Math.max(a.x(), b.x()), Math.max(a.y(), b.y()));
    }

    public static Bounds of(Collection<Vec2> points) {
        if (points.isEmpty()) {
            throw new IllegalArgumentException("no points");
        }
        double x0 = Double.POSITIVE_INFINITY, y0 = Double.POSITIVE_INFINITY;
        double x1 = Double.NEGATIVE_INFINITY, y1 = Double.NEGATIVE_INFINITY;
        for (Vec2 p : points) {
            x0 = Math.min(x0, p.x());
            y0 = Math.min(y0, p.y());
            x1 = Math.max(x1, p.x());
            y1 = Math.max(y1, p.y());
        }
        return new Bounds(x0, y0, x1, y1);
    }

    public double width() {
        return maxX - minX;
    }

    public double height() {
        return maxY - minY;
    }

    public Vec2 center() {
        return new Vec2((minX + maxX) / 2, (minY + maxY) / 2);
    }

    public boolean contains(Vec2 p) {
        return p.x() >= minX && p.x() <= maxX && p.y() >= minY && p.y() <= maxY;
    }

    public boolean contains(Bounds o) {
        return o.minX >= minX && o.maxX <= maxX && o.minY >= minY && o.maxY <= maxY;
    }

    public boolean intersects(Bounds o) {
        return o.minX <= maxX && o.maxX >= minX && o.minY <= maxY && o.maxY >= minY;
    }

    public Bounds union(Bounds o) {
        return new Bounds(Math.min(minX, o.minX), Math.min(minY, o.minY),
                Math.max(maxX, o.maxX), Math.max(maxY, o.maxY));
    }

    public Bounds inflate(double d) {
        return new Bounds(minX - d, minY - d, maxX + d, maxY + d);
    }
}
