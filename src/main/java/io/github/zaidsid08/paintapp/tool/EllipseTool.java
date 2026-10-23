// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.tool;

import io.github.zaidsid08.paintapp.model.Bounds;
import io.github.zaidsid08.paintapp.model.Element;
import io.github.zaidsid08.paintapp.model.EllipseShape;
import io.github.zaidsid08.paintapp.model.Style;
import io.github.zaidsid08.paintapp.model.Vec2;

/** Ellipses. Shift makes a circle. */
public final class EllipseTool extends DragShapeTool {

    public EllipseTool(ToolContext ctx) {
        super(ctx);
    }

    @Override
    public String name() {
        return "Ellipse";
    }

    @Override
    public String hint() {
        return "Drag to draw an ellipse. Shift for a circle";
    }

    @Override
    protected Element create(long id, Vec2 a, Vec2 b, boolean constrain, Style style) {
        return new EllipseShape(id, Bounds.spanning(a, constrain ? squareCorner(a, b) : b), style);
    }
}
