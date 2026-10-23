// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns a chain of points into smooth cubic Bézier segments using a
 * Catmull-Rom spline, which passes through every input point. Both the
 * on-screen renderer and the SVG exporter use this, so they draw identical
 * curves.
 */
public final class Smoothing {

    /** One cubic Bézier segment from {@code from} to {@code to}. */
    public record Cubic(Vec2 from, Vec2 c1, Vec2 c2, Vec2 to) {
    }

    private Smoothing() {
    }

    public static List<Cubic> catmullRom(List<Vec2> pts) {
        List<Cubic> out = new ArrayList<>();
        for (int i = 0; i + 1 < pts.size(); i++) {
            Vec2 p0 = pts.get(Math.max(0, i - 1));
            Vec2 p1 = pts.get(i);
            Vec2 p2 = pts.get(i + 1);
            Vec2 p3 = pts.get(Math.min(pts.size() - 1, i + 2));
            // Standard Catmull-Rom to Bézier conversion (tension 0.5).
            Vec2 c1 = p1.plus(p2.minus(p0).times(1.0 / 6));
            Vec2 c2 = p2.minus(p3.minus(p1).times(1.0 / 6));
            out.add(new Cubic(p1, c1, c2, p2));
        }
        return out;
    }
}
