// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.tool;

import io.github.zaidsid08.paintapp.command.RemoveElements;
import io.github.zaidsid08.paintapp.model.Element;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Drag across elements to erase them. Everything touched during one drag is
 * removed by a single command, so one undo brings it all back.
 */
public final class EraserTool implements Tool {

    private final ToolContext ctx;
    private final Set<Long> marked = new LinkedHashSet<>();
    private boolean active;

    public EraserTool(ToolContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public String name() {
        return "Eraser";
    }

    @Override
    public String hint() {
        return "Drag over elements to erase them";
    }

    @Override
    public void pressed(PointerEvent e) {
        active = true;
        marked.clear();
        markUnder(e);
    }

    @Override
    public void dragged(PointerEvent e) {
        if (active) {
            markUnder(e);
        }
    }

    @Override
    public void released(PointerEvent e) {
        if (!active) {
            return;
        }
        active = false;
        ctx.setHighlighted(Set.of());
        if (!marked.isEmpty()) {
            ctx.selection().clear();
            ctx.history().execute(new RemoveElements(marked));
        }
        marked.clear();
    }

    @Override
    public void deactivate() {
        active = false;
        marked.clear();
        ctx.setHighlighted(Set.of());
    }

    private void markUnder(PointerEvent e) {
        boolean changed = false;
        for (Element el : ctx.document().elements()) {
            if (el.hit(e.position(), ctx.hitTolerance()) && marked.add(el.id())) {
                changed = true;
            }
        }
        if (changed) {
            ctx.setHighlighted(Set.copyOf(marked));
        }
    }
}
