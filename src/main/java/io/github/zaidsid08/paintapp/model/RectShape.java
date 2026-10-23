// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

import java.util.List;

/** An axis-aligned rectangle. */
public record RectShape(long id, Bounds box, Style style) implements Element {

    @Override
    public Bounds bounds() {
        return box;
    }

    @Override
    public boolean hit(Vec2 p, double tolerance) {
        if (style.fill() != null && box.contains(p)) {
            return true;
        }
        return Geometry.distanceToPolyline(p, corners(), true) <= reach(tolerance);
    }

    public List<Vec2> corners() {
        return List.of(new Vec2(box.minX(), box.minY()), new Vec2(box.maxX(), box.minY()),
                new Vec2(box.maxX(), box.maxY()), new Vec2(box.minX(), box.maxY()));
    }

    @Override
    public RectShape translate(Vec2 d) {
        return new RectShape(id, new Bounds(box.minX() + d.x(), box.minY() + d.y(),
                box.maxX() + d.x(), box.maxY() + d.y()), style);
    }

    @Override
    public RectShape withStyle(Style s) {
        return new RectShape(id, box, s);
    }

    @Override
    public RectShape withId(long newId) {
        return new RectShape(newId, box, style);
    }
}
