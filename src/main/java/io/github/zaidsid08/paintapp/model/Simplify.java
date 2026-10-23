// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Ramer–Douglas–Peucker polyline simplification.
 *
 * <p>Freehand input arrives as one point per mouse event, which is hundreds of
 * points for a short stroke. RDP keeps only the points that matter to the
 * shape: a point is dropped if it lies within {@code epsilon} of the segment
 * joining its retained neighbours.
 */
public final class Simplify {

    private Simplify() {
    }

    public static List<Vec2> rdp(List<Vec2> points, double epsilon) {
        if (points.size() < 3) {
            return List.copyOf(points);
        }
        boolean[] keep = new boolean[points.size()];
        keep[0] = true;
        keep[points.size() - 1] = true;

        // Iterative with an explicit stack so long strokes can't overflow the call stack.
        ArrayList<int[]> stack = new ArrayList<>();
        stack.add(new int[] {0, points.size() - 1});
        while (!stack.isEmpty()) {
            int[] range = stack.remove(stack.size() - 1);
            int first = range[0], last = range[1];
            double maxDist = -1;
            int index = -1;
            for (int i = first + 1; i < last; i++) {
                double d = Geometry.distanceToSegment(points.get(i), points.get(first), points.get(last));
                if (d > maxDist) {
                    maxDist = d;
                    index = i;
                }
            }
            if (index != -1 && maxDist > epsilon) {
                keep[index] = true;
                stack.add(new int[] {first, index});
                stack.add(new int[] {index, last});
            }
        }

        List<Vec2> out = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            if (keep[i]) {
                out.add(points.get(i));
            }
        }
        return List.copyOf(out);
    }
}
