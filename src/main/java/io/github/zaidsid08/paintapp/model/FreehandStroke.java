// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

import java.util.List;

/** A pen stroke: an open chain of points, drawn smoothed. Never filled. */
public record FreehandStroke(long id, List<Vec2> points, Style style) implements Element {

    public FreehandStroke {
        points = List.copyOf(points);
        if (points.isEmpty()) {
            throw new IllegalArgumentException("a stroke needs at least one point");
        }
    }

    @Override
    public Bounds bounds() {
        return Bounds.of(points);
    }

    @Override
    public boolean hit(Vec2 p, double tolerance) {
        return Geometry.distanceToPolyline(p, points, false) <= reach(tolerance);
    }

    @Override
    public FreehandStroke translate(Vec2 d) {
        return new FreehandStroke(id, points.stream().map(q -> q.plus(d)).toList(), style);
    }

    @Override
    public FreehandStroke withStyle(Style s) {
        return new FreehandStroke(id, points, s);
    }

    @Override
    public FreehandStroke withId(long newId) {
        return new FreehandStroke(newId, points, style);
    }
}
