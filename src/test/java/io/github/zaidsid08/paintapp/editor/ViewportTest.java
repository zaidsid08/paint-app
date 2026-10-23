// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.zaidsid08.paintapp.model.Vec2;
import org.junit.jupiter.api.Test;

class ViewportTest {

    private static void assertClose(Vec2 expected, Vec2 actual) {
        assertEquals(expected.x(), actual.x(), 1e-9);
        assertEquals(expected.y(), actual.y(), 1e-9);
    }

    @Test
    void screenAndDocumentAreInverses() {
        Viewport v = new Viewport();
        v.panBy(30, -12);
        v.zoomAt(new Vec2(100, 100), 3);
        Vec2 p = new Vec2(17.5, -4);
        assertClose(p, v.toDocument(v.toScreen(p)));
    }

    @Test
    void zoomKeepsThePointUnderTheCursorFixed() {
        Viewport v = new Viewport();
        Vec2 cursor = new Vec2(240, 130);
        Vec2 before = v.toDocument(cursor);
        v.zoomAt(cursor, 2.5);
        v.zoomAt(cursor, 0.7);
        assertClose(before, v.toDocument(cursor));
    }

    @Test
    void zoomIsClamped() {
        Viewport v = new Viewport();
        v.zoomAt(Vec2.ZERO, 1000);
        assertEquals(Viewport.MAX_SCALE, v.scale());
        v.zoomAt(Vec2.ZERO, 1e-6);
        assertEquals(Viewport.MIN_SCALE, v.scale());
    }
}
