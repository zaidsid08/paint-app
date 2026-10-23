// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.tool;

import io.github.zaidsid08.paintapp.model.Bounds;
import io.github.zaidsid08.paintapp.model.Element;
import io.github.zaidsid08.paintapp.model.RectShape;
import io.github.zaidsid08.paintapp.model.Style;
import io.github.zaidsid08.paintapp.model.Vec2;

/** Rectangles. Shift makes a square. */
public final class RectTool extends DragShapeTool {

    public RectTool(ToolContext ctx) {
        super(ctx);
    }

    @Override
    public String name() {
        return "Rectangle";
    }

    @Override
    public String hint() {
        return "Drag to draw a rectangle. Shift for a square";
    }

    @Override
    protected Element create(long id, Vec2 a, Vec2 b, boolean constrain, Style style) {
        return new RectShape(id, Bounds.spanning(a, constrain ? squareCorner(a, b) : b), style);
    }
}
