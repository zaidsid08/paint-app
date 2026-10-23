// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

import java.util.List;

/** A polyline drawn vertex by vertex; when closed it becomes a polygon and can be filled. */
public record PolyShape(long id, List<Vec2> vertices, boolean closed, Style style) implements Element {

    public PolyShape {
        vertices = List.copyOf(vertices);
        if (vertices.size() < 2) {
            throw new IllegalArgumentException("a polyline needs at least two vertices");
        }
    }

    @Override
    public Bounds bounds() {
        return Bounds.of(vertices);
    }

    @Override
    public boolean hit(Vec2 p, double tolerance) {
        if (closed && style.fill() != null && Geometry.insidePolygon(p, vertices)) {
            return true;
        }
        return Geometry.distanceToPolyline(p, vertices, closed) <= reach(tolerance);
    }

    @Override
    public PolyShape translate(Vec2 d) {
        return new PolyShape(id, vertices.stream().map(v -> v.plus(d)).toList(), closed, style);
    }

    @Override
    public PolyShape withStyle(Style s) {
        return new PolyShape(id, vertices, closed, s);
    }

    @Override
    public PolyShape withId(long newId) {
        return new PolyShape(newId, vertices, closed, style);
    }
}
