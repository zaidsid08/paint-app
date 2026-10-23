// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.command;

import io.github.zaidsid08.paintapp.model.Element;
import io.github.zaidsid08.paintapp.model.EllipseShape;
import io.github.zaidsid08.paintapp.model.FreehandStroke;
import io.github.zaidsid08.paintapp.model.LineSegment;
import io.github.zaidsid08.paintapp.model.PolyShape;
import io.github.zaidsid08.paintapp.model.RectShape;

/** Human-readable names for undo labels. */
final class Labels {

    private Labels() {
    }

    static String kindOf(Element e) {
        return switch (e) {
            case FreehandStroke s -> "stroke";
            case LineSegment l -> "line";
            case RectShape r -> "rectangle";
            case EllipseShape el -> "ellipse";
            case PolyShape p -> p.closed() ? "polygon" : "polyline";
        };
    }
}
