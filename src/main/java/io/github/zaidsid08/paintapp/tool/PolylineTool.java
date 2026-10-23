// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.tool;

import io.github.zaidsid08.paintapp.command.AddElements;
import io.github.zaidsid08.paintapp.model.PolyShape;
import io.github.zaidsid08.paintapp.model.Vec2;
import java.util.ArrayList;
import java.util.List;

/**
 * Click-by-click polylines. Click to add vertices, then either click the
 * first vertex to close the shape into a polygon, or double-click / press
 * Enter to finish it open. Escape cancels.
 */
public final class PolylineTool implements Tool {

    /** How close (as a multiple of the hit tolerance) a click must be to the start to close. */
    static final double CLOSE_RADIUS_FACTOR = 2.0;

    private final ToolContext ctx;
    private final List<Vec2> vertices = new ArrayList<>();
    private Vec2 hover;

    public PolylineTool(ToolContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public String name() {
        return "Polyline";
    }

    @Override
    public String hint() {
        return vertices.isEmpty()
                ? "Click to place points"
                : "Click to add a point, click the first point to close, double-click or Enter to finish, Esc to cancel";
    }

    @Override
    public void pressed(PointerEvent e) {
        Vec2 p = e.position();
        if (e.clickCount() >= 2) {
            finish(false);
            return;
        }
        if (vertices.size() >= 3 && p.distanceTo(vertices.get(0)) <= closeRadius()) {
            finish(true);
            return;
        }
        vertices.add(p);
        hover = p;
        showPreview();
    }

    @Override
    public void moved(PointerEvent e) {
        if (!vertices.isEmpty()) {
            hover = e.position();
            showPreview();
        }
    }

    @Override
    public void key(Key key, boolean shift) {
        if (key == Key.ENTER) {
            finish(false);
        } else if (key == Key.ESCAPE) {
            cancel();
        }
    }

    @Override
    public void deactivate() {
        finish(false);
    }

    boolean inProgress() {
        return !vertices.isEmpty();
    }

    private double closeRadius() {
        return ctx.hitTolerance() * CLOSE_RADIUS_FACTOR;
    }

    private void finish(boolean closed) {
        if (vertices.size() >= 2) {
            long id = ctx.document().newId();
            ctx.history().execute(AddElements.one(new PolyShape(id, vertices, closed, ctx.currentStyle())));
        }
        cancel();
    }

    private void cancel() {
        vertices.clear();
        hover = null;
        ctx.setPreview(null);
    }

    private void showPreview() {
        List<Vec2> pts = new ArrayList<>(vertices);
        if (hover != null && !hover.equals(pts.get(pts.size() - 1))) {
            pts.add(hover);
        }
        ctx.setPreview(pts.size() >= 2 ? new PolyShape(0, pts, false, ctx.currentStyle()) : null);
    }
}
