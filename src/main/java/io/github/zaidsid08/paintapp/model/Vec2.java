// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

/** An immutable 2D point or vector in document coordinates. */
public record Vec2(double x, double y) {

    public static final Vec2 ZERO = new Vec2(0, 0);

    public Vec2 plus(Vec2 o) {
        return new Vec2(x + o.x, y + o.y);
    }

    public Vec2 minus(Vec2 o) {
        return new Vec2(x - o.x, y - o.y);
    }

    public Vec2 times(double k) {
        return new Vec2(x * k, y * k);
    }

    public double dot(Vec2 o) {
        return x * o.x + y * o.y;
    }

    public double length() {
        return Math.hypot(x, y);
    }

    public double distanceTo(Vec2 o) {
        return Math.hypot(x - o.x, y - o.y);
    }
}
