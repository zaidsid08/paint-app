// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

import java.util.List;

/** Small geometry helpers used for hit-testing. */
public final class Geometry {

    private Geometry() {
    }

    /** Shortest distance from p to the segment ab. */
    public static double distanceToSegment(Vec2 p, Vec2 a, Vec2 b) {
        Vec2 ab = b.minus(a);
        double len2 = ab.dot(ab);
        if (len2 == 0) {
            return p.distanceTo(a);
        }
        double t = Math.max(0, Math.min(1, p.minus(a).dot(ab) / len2));
        return p.distanceTo(a.plus(ab.times(t)));
    }

    /** Shortest distance from p to a chain of segments. */
    public static double distanceToPolyline(Vec2 p, List<Vec2> pts, boolean closed) {
        if (pts.size() == 1) {
            return p.distanceTo(pts.get(0));
        }
        double best = Double.POSITIVE_INFINITY;
        for (int i = 0; i + 1 < pts.size(); i++) {
            best = Math.min(best, distanceToSegment(p, pts.get(i), pts.get(i + 1)));
        }
        if (closed && pts.size() > 2) {
            best = Math.min(best, distanceToSegment(p, pts.get(pts.size() - 1), pts.get(0)));
        }
        return best;
    }

    /** Even-odd point-in-polygon test (ray casting). */
    public static boolean insidePolygon(Vec2 p, List<Vec2> pts) {
        boolean inside = false;
        for (int i = 0, j = pts.size() - 1; i < pts.size(); j = i++) {
            Vec2 a = pts.get(i), b = pts.get(j);
            boolean crosses = (a.y() > p.y()) != (b.y() > p.y());
            if (crosses) {
                double xAtY = a.x() + (p.y() - a.y()) * (b.x() - a.x()) / (b.y() - a.y());
                if (p.x() < xAtY) {
                    inside = !inside;
                }
            }
        }
        return inside;
    }
}
