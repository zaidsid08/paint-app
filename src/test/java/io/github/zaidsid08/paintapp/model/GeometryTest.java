// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class GeometryTest {

    @Test
    void distanceToSegmentUsesPerpendicularInsideAndEndpointsOutside() {
        Vec2 a = new Vec2(0, 0), b = new Vec2(10, 0);
        assertEquals(3, Geometry.distanceToSegment(new Vec2(5, 3), a, b), 1e-9);
        assertEquals(5, Geometry.distanceToSegment(new Vec2(13, 4), a, b), 1e-9);
        assertEquals(2, Geometry.distanceToSegment(new Vec2(-2, 0), a, b), 1e-9);
    }

    @Test
    void degenerateSegmentIsAPoint() {
        Vec2 p = new Vec2(1, 1);
        assertEquals(Math.sqrt(2), Geometry.distanceToSegment(new Vec2(2, 2), p, p), 1e-9);
    }

    @Test
    void pointInPolygonHandlesConcaveShapes() {
        // A "U" shape: the notch at the top middle is outside.
        List<Vec2> u = List.of(new Vec2(0, 0), new Vec2(3, 0), new Vec2(3, 3), new Vec2(2, 3),
                new Vec2(2, 1), new Vec2(1, 1), new Vec2(1, 3), new Vec2(0, 3));
        assertTrue(Geometry.insidePolygon(new Vec2(0.5, 2), u));
        assertTrue(Geometry.insidePolygon(new Vec2(2.5, 2), u));
        assertFalse(Geometry.insidePolygon(new Vec2(1.5, 2), u));
        assertFalse(Geometry.insidePolygon(new Vec2(5, 1), u));
    }

    @Test
    void boundsSpanningIsOrderIndependent() {
        Bounds b = Bounds.spanning(new Vec2(10, 2), new Vec2(4, 8));
        assertEquals(new Bounds(4, 2, 10, 8), b);
        assertEquals(6, b.width());
        assertTrue(b.contains(new Vec2(4, 8)));
        assertTrue(b.intersects(new Bounds(9, 7, 20, 20)));
        assertFalse(b.intersects(new Bounds(11, 0, 20, 1)));
        assertThrows(IllegalArgumentException.class, () -> new Bounds(1, 0, 0, 1));
    }

    @Test
    void rgbaHexRoundTrip() {
        Rgba c = Rgba.fromHex("#1e66f580");
        assertEquals("#1e66f580", c.toHex());
        assertEquals("#1e66f5", c.toHexRgb());
        assertEquals(1.0, Rgba.fromHex("ff0000").a());
        assertThrows(IllegalArgumentException.class, () -> Rgba.fromHex("#12"));
        assertThrows(IllegalArgumentException.class, () -> Rgba.fromHex("#zzzzzz"));
    }
}
