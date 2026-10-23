// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ElementTest {

    private static final Style OUTLINE = new Style(Rgba.BLACK, null, 2);
    private static final Style FILLED = new Style(Rgba.BLACK, Rgba.WHITE, 2);
    private static final double TOL = 1;

    @Test
    void unfilledRectIsOnlyHitNearItsEdges() {
        RectShape r = new RectShape(1, new Bounds(0, 0, 100, 50), OUTLINE);
        assertTrue(r.hit(new Vec2(50, 0.5), TOL));
        assertTrue(r.hit(new Vec2(101.5, 25), TOL)); // tolerance + half stroke width
        assertFalse(r.hit(new Vec2(50, 25), TOL));
        assertTrue(r.withStyle(FILLED).hit(new Vec2(50, 25), TOL));
    }

    @Test
    void ellipseHitTestDistinguishesOutlineInteriorAndOutside() {
        EllipseShape e = new EllipseShape(1, new Bounds(0, 0, 100, 50), OUTLINE);
        assertTrue(e.hit(new Vec2(100, 25), TOL));
        assertTrue(e.hit(new Vec2(50, 0), TOL));
        assertFalse(e.hit(new Vec2(50, 25), TOL));
        assertFalse(e.hit(new Vec2(2, 2), TOL)); // inside the box, outside the ellipse
        assertTrue(e.withStyle(FILLED).hit(new Vec2(50, 25), TOL));
    }

    @Test
    void openPolylineIgnoresFillButClosedPolygonUsesIt() {
        List<Vec2> tri = List.of(new Vec2(0, 0), new Vec2(40, 0), new Vec2(20, 30));
        PolyShape open = new PolyShape(1, tri, false, FILLED);
        PolyShape closed = new PolyShape(2, tri, true, FILLED);
        Vec2 centre = new Vec2(20, 10);
        assertFalse(open.hit(centre, TOL));
        assertTrue(closed.hit(centre, TOL));
        // The closing edge only exists when closed.
        assertFalse(open.hit(new Vec2(10, 15), TOL));
        assertTrue(closed.hit(new Vec2(10, 15), TOL));
    }

    @Test
    void translateAndRestyleKeepIdentity() {
        LineSegment l = new LineSegment(7, new Vec2(0, 0), new Vec2(10, 0), OUTLINE);
        LineSegment moved = l.translate(new Vec2(5, 5));
        assertEquals(7, moved.id());
        assertEquals(new Vec2(5, 5), moved.start());
        assertEquals(7, moved.withStyle(FILLED).id());
        assertEquals(9, moved.withId(9).id());
    }

    @Test
    void invalidShapesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FreehandStroke(1, List.of(), OUTLINE));
        assertThrows(IllegalArgumentException.class, () -> new PolyShape(1, List.of(Vec2.ZERO), false, OUTLINE));
        assertThrows(IllegalArgumentException.class, () -> new Style(Rgba.BLACK, null, 0));
    }

    @Test
    void paintedBoundsIncludeHalfTheStroke() {
        RectShape r = new RectShape(1, new Bounds(0, 0, 10, 10), new Style(Rgba.BLACK, null, 4));
        assertEquals(new Bounds(-2, -2, 12, 12), r.paintedBounds());
    }
}
