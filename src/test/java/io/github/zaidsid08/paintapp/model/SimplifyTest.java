// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SimplifyTest {

    @Test
    void collinearPointsCollapseToEndpoints() {
        List<Vec2> line = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            line.add(new Vec2(i, 2 * i));
        }
        assertEquals(List.of(new Vec2(0, 0), new Vec2(100, 200)), Simplify.rdp(line, 0.5));
    }

    @Test
    void cornersAreKept() {
        List<Vec2> pts = List.of(new Vec2(0, 0), new Vec2(5, 0), new Vec2(10, 0),
                new Vec2(10, 5), new Vec2(10, 10));
        assertEquals(List.of(new Vec2(0, 0), new Vec2(10, 0), new Vec2(10, 10)), Simplify.rdp(pts, 0.1));
    }

    @Test
    void everyDroppedPointStaysWithinEpsilonOfTheResult() {
        List<Vec2> wave = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            wave.add(new Vec2(i * 0.5, 20 * Math.sin(i / 40.0)));
        }
        double eps = 0.75;
        List<Vec2> out = Simplify.rdp(wave, eps);
        assertTrue(out.size() < wave.size() / 5, "should drop most points, kept " + out.size());
        for (Vec2 p : wave) {
            assertTrue(Geometry.distanceToPolyline(p, out, false) <= eps + 1e-9);
        }
    }

    @Test
    void tinyInputsAreReturnedAsIs() {
        assertEquals(List.of(), Simplify.rdp(List.of(), 1));
        assertEquals(List.of(new Vec2(1, 1), new Vec2(2, 2)), Simplify.rdp(List.of(new Vec2(1, 1), new Vec2(2, 2)), 1));
    }
}
