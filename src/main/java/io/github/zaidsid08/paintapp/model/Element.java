// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

/**
 * Something drawn on the canvas. Elements are immutable: moving or restyling
 * one produces a new instance with the same {@link #id()}. That makes undo
 * trivial, since a command just swaps the old instance for the new one.
 */
public sealed interface Element
        permits FreehandStroke, LineSegment, RectShape, EllipseShape, PolyShape {

    /** Stable identity used by selection and commands. */
    long id();

    Style style();

    /** Geometric bounds, not counting stroke width. */
    Bounds bounds();

    /** True if p is on the element, within tolerance (plus half the stroke width). */
    boolean hit(Vec2 p, double tolerance);

    Element translate(Vec2 delta);

    Element withStyle(Style style);

    /** A copy with a different id, used when duplicating. */
    Element withId(long newId);

    /** Bounds including the painted stroke. */
    default Bounds paintedBounds() {
        return bounds().inflate(style().strokeWidth() / 2);
    }

    /** Distance below which a point counts as touching the outline. */
    default double reach(double tolerance) {
        return tolerance + style().strokeWidth() / 2;
    }
}
