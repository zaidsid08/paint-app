// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.tool;

import io.github.zaidsid08.paintapp.command.ReplaceElements;
import io.github.zaidsid08.paintapp.model.Bounds;
import io.github.zaidsid08.paintapp.model.Element;
import io.github.zaidsid08.paintapp.model.Vec2;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Click to select, Shift-click to add/remove, drag a selected element to move
 * the whole selection, or drag on empty space for a marquee. Arrow keys nudge
 * by 1 unit (10 with Shift).
 */
public final class SelectTool implements Tool {

    private enum Mode { IDLE, MOVING, MARQUEE }

    private final ToolContext ctx;
    private Mode mode = Mode.IDLE;
    private Vec2 start;
    private boolean additive;
    private List<Element> originals = List.of();

    public SelectTool(ToolContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public String name() {
        return "Select";
    }

    @Override
    public String hint() {
        return "Click to select, Shift-click to add, drag to move, drag on empty space to box-select";
    }

    @Override
    public void pressed(PointerEvent e) {
        start = e.position();
        additive = e.shift();
        Optional<Element> hit = ctx.document().topmostAt(start, ctx.hitTolerance());

        if (hit.isEmpty()) {
            if (!additive) {
                ctx.selection().clear();
            }
            mode = Mode.MARQUEE;
            return;
        }
        long id = hit.get().id();
        if (additive) {
            ctx.selection().toggle(id);
            mode = Mode.IDLE;
            return;
        }
        if (!ctx.selection().contains(id)) {
            ctx.selection().select(id);
        }
        originals = ctx.selection().elementsIn(ctx.document());
        mode = Mode.MOVING;
    }

    @Override
    public void dragged(PointerEvent e) {
        switch (mode) {
            case MOVING -> {
                // Live preview: edit the document directly, then undo that
                // and go through the history on release.
                Vec2 delta = e.position().minus(start);
                originals.forEach(o -> ctx.document().replace(o.translate(delta)));
            }
            case MARQUEE -> ctx.setMarquee(Bounds.spanning(start, e.position()));
            case IDLE -> { }
        }
    }

    @Override
    public void released(PointerEvent e) {
        switch (mode) {
            case MOVING -> commitMove(e.position().minus(start));
            case MARQUEE -> {
                ctx.setMarquee(null);
                Bounds box = Bounds.spanning(start, e.position());
                Set<Long> ids = new LinkedHashSet<>();
                if (additive) {
                    ids.addAll(ctx.selection().ids());
                }
                ctx.document().within(box).forEach(el -> ids.add(el.id()));
                ctx.selection().set(ids);
            }
            case IDLE -> { }
        }
        mode = Mode.IDLE;
        originals = List.of();
    }

    @Override
    public void key(Key key, boolean shift) {
        double step = shift ? 10 : 1;
        Vec2 delta = switch (key) {
            case LEFT -> new Vec2(-step, 0);
            case RIGHT -> new Vec2(step, 0);
            case UP -> new Vec2(0, -step);
            case DOWN -> new Vec2(0, step);
            case ESCAPE -> {
                ctx.selection().clear();
                yield null;
            }
            default -> null;
        };
        if (delta != null && !ctx.selection().isEmpty() && mode == Mode.IDLE) {
            originals = ctx.selection().elementsIn(ctx.document());
            commitMove(delta);
            originals = List.of();
        }
    }

    @Override
    public void deactivate() {
        if (mode == Mode.MOVING) {
            originals.forEach(ctx.document()::replace);
        }
        ctx.setMarquee(null);
        mode = Mode.IDLE;
        originals = List.of();
    }

    private void commitMove(Vec2 delta) {
        originals.forEach(ctx.document()::replace); // undo the live preview
        if (delta.x() == 0 && delta.y() == 0) {
            return;
        }
        List<Element> moved = originals.stream().map(o -> o.translate(delta)).toList();
        String label = moved.size() == 1 ? "Move" : "Move " + moved.size() + " elements";
        ctx.history().execute(new ReplaceElements(originals, moved, label));
    }
}
