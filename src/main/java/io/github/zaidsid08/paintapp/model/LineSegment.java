// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

/** A straight line between two points. */
public record LineSegment(long id, Vec2 start, Vec2 end, Style style) implements Element {

    @Override
    public Bounds bounds() {
        return Bounds.spanning(start, end);
    }

    @Override
    public boolean hit(Vec2 p, double tolerance) {
        return Geometry.distanceToSegment(p, start, end) <= reach(tolerance);
    }

    @Override
    public LineSegment translate(Vec2 d) {
        return new LineSegment(id, start.plus(d), end.plus(d), style);
    }

    @Override
    public LineSegment withStyle(Style s) {
        return new LineSegment(id, start, end, s);
    }

    @Override
    public LineSegment withId(long newId) {
        return new LineSegment(newId, start, end, style);
    }
}
