// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.editor;

import io.github.zaidsid08.paintapp.model.Vec2;

/**
 * Maps between document coordinates and screen pixels:
 * {@code screen = document * scale + offset}. Zoom and pan only change this
 * mapping; the document itself is never touched.
 */
public final class Viewport {

    public static final double MIN_SCALE = 0.1;
    public static final double MAX_SCALE = 16;

    private double scale = 1;
    private double offsetX;
    private double offsetY;

    public double scale() {
        return scale;
    }

    public Vec2 toScreen(Vec2 doc) {
        return new Vec2(doc.x() * scale + offsetX, doc.y() * scale + offsetY);
    }

    public Vec2 toDocument(Vec2 screen) {
        return new Vec2((screen.x() - offsetX) / scale, (screen.y() - offsetY) / scale);
    }

    /** Zoom by factor while keeping the document point under {@code screenAnchor} fixed. */
    public void zoomAt(Vec2 screenAnchor, double factor) {
        Vec2 anchorDoc = toDocument(screenAnchor);
        scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale * factor));
        offsetX = screenAnchor.x() - anchorDoc.x() * scale;
        offsetY = screenAnchor.y() - anchorDoc.y() * scale;
    }

    public void panBy(double dx, double dy) {
        offsetX += dx;
        offsetY += dy;
    }

    public void reset() {
        scale = 1;
        offsetX = 0;
        offsetY = 0;
    }

    public double offsetX() {
        return offsetX;
    }

    public double offsetY() {
        return offsetY;
    }
}
