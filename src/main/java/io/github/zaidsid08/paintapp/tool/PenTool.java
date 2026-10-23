// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.tool;

import io.github.zaidsid08.paintapp.command.AddElements;
import io.github.zaidsid08.paintapp.model.FreehandStroke;
import io.github.zaidsid08.paintapp.model.Simplify;
import io.github.zaidsid08.paintapp.model.Vec2;
import java.util.ArrayList;
import java.util.List;

/** Freehand drawing. The raw input is simplified with RDP when the stroke ends. */
public final class PenTool implements Tool {

    /** Max deviation (document units) allowed when simplifying a finished stroke. */
    static final double SIMPLIFY_EPSILON = 0.75;
    private static final double MIN_STEP = 0.5;

    private final ToolContext ctx;
    private final List<Vec2> points = new ArrayList<>();

    public PenTool(ToolContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public String name() {
        return "Pen";
    }

    @Override
    public String hint() {
        return "Drag to draw freehand";
    }

    @Override
    public void pressed(PointerEvent e) {
        points.clear();
        points.add(e.position());
        showPreview();
    }

    @Override
    public void dragged(PointerEvent e) {
        if (points.isEmpty()) {
            return;
        }
        if (e.position().distanceTo(points.get(points.size() - 1)) >= MIN_STEP) {
            points.add(e.position());
            showPreview();
        }
    }

    @Override
    public void released(PointerEvent e) {
        if (points.isEmpty()) {
            return;
        }
        List<Vec2> simplified = Simplify.rdp(points, SIMPLIFY_EPSILON);
        points.clear();
        ctx.setPreview(null);
        long id = ctx.document().newId();
        ctx.history().execute(AddElements.one(new FreehandStroke(id, simplified, ctx.currentStyle())));
    }

    @Override
    public void deactivate() {
        points.clear();
        ctx.setPreview(null);
    }

    private void showPreview() {
        ctx.setPreview(new FreehandStroke(0, points, ctx.currentStyle()));
    }
}
