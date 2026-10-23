// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.tool;

import io.github.zaidsid08.paintapp.command.AddElements;
import io.github.zaidsid08.paintapp.model.Element;
import io.github.zaidsid08.paintapp.model.Style;
import io.github.zaidsid08.paintapp.model.Vec2;

/**
 * Base for tools that draw by dragging from one corner to another
 * (line, rectangle, ellipse). Subclasses only decide how two points become an
 * element; press/drag/preview/commit is shared here.
 */
public abstract class DragShapeTool implements Tool {

    /** Drags shorter than this in both directions are treated as accidental clicks. */
    static final double MIN_SIZE = 1.0;

    protected final ToolContext ctx;
    private Vec2 anchor;

    protected DragShapeTool(ToolContext ctx) {
        this.ctx = ctx;
    }

    /** Build the element for a drag from a to b. With constrain set, hold Shift behaviour. */
    protected abstract Element create(long id, Vec2 a, Vec2 b, boolean constrain, Style style);

    @Override
    public void pressed(PointerEvent e) {
        anchor = e.position();
    }

    @Override
    public void dragged(PointerEvent e) {
        if (anchor != null) {
            ctx.setPreview(create(0, anchor, e.position(), e.shift(), ctx.currentStyle()));
        }
    }

    @Override
    public void released(PointerEvent e) {
        if (anchor == null) {
            return;
        }
        Vec2 a = anchor;
        anchor = null;
        ctx.setPreview(null);
        Element shape = create(ctx.document().newId(), a, e.position(), e.shift(), ctx.currentStyle());
        if (shape.bounds().width() < MIN_SIZE && shape.bounds().height() < MIN_SIZE) {
            return;
        }
        ctx.history().execute(AddElements.one(shape));
    }

    @Override
    public void deactivate() {
        anchor = null;
        ctx.setPreview(null);
    }

    /** Moves b so that a-b spans a square, keeping the drag direction. */
    protected static Vec2 squareCorner(Vec2 a, Vec2 b) {
        double dx = b.x() - a.x(), dy = b.y() - a.y();
        double side = Math.max(Math.abs(dx), Math.abs(dy));
        return new Vec2(a.x() + Math.copySign(side, dx), a.y() + Math.copySign(side, dy));
    }
}
