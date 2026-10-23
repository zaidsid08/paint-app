// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

/** An axis-aligned ellipse inscribed in a bounding box. */
public record EllipseShape(long id, Bounds box, Style style) implements Element {

    @Override
    public Bounds bounds() {
        return box;
    }

    @Override
    public boolean hit(Vec2 p, double tolerance) {
        double rx = box.width() / 2, ry = box.height() / 2;
        Vec2 c = box.center();
        if (rx == 0 || ry == 0) {
            // Degenerate ellipse: treat as the line it collapses to.
            return Geometry.distanceToSegment(p, new Vec2(box.minX(), box.minY()),
                    new Vec2(box.maxX(), box.maxY())) <= reach(tolerance);
        }
        double nx = (p.x() - c.x()) / rx, ny = (p.y() - c.y()) / ry;
        double r = Math.sqrt(nx * nx + ny * ny); // 1.0 exactly on the outline
        if (style.fill() != null && r <= 1) {
            return true;
        }
        // Approximate distance to the outline, scaled back to document units.
        return Math.abs(r - 1) * Math.min(rx, ry) <= reach(tolerance);
    }

    @Override
    public EllipseShape translate(Vec2 d) {
        return new EllipseShape(id, new Bounds(box.minX() + d.x(), box.minY() + d.y(),
                box.maxX() + d.x(), box.maxY() + d.y()), style);
    }

    @Override
    public EllipseShape withStyle(Style s) {
        return new EllipseShape(id, box, s);
    }

    @Override
    public EllipseShape withId(long newId) {
        return new EllipseShape(newId, box, style);
    }
}
