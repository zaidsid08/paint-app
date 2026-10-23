// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.tool;

import io.github.zaidsid08.paintapp.model.Element;
import io.github.zaidsid08.paintapp.model.LineSegment;
import io.github.zaidsid08.paintapp.model.Style;
import io.github.zaidsid08.paintapp.model.Vec2;

/** Straight lines. Shift snaps the angle to multiples of 45°. */
public final class LineTool extends DragShapeTool {

    public LineTool(ToolContext ctx) {
        super(ctx);
    }

    @Override
    public String name() {
        return "Line";
    }

    @Override
    public String hint() {
        return "Drag to draw a line. Shift snaps to 45°";
    }

    @Override
    protected Element create(long id, Vec2 a, Vec2 b, boolean constrain, Style style) {
        return new LineSegment(id, a, constrain ? snapAngle(a, b) : b, style);
    }

    static Vec2 snapAngle(Vec2 a, Vec2 b) {
        Vec2 d = b.minus(a);
        double step = Math.PI / 4;
        double angle = Math.round(Math.atan2(d.y(), d.x()) / step) * step;
        double len = d.length();
        return new Vec2(a.x() + len * Math.cos(angle), a.y() + len * Math.sin(angle));
    }
}
