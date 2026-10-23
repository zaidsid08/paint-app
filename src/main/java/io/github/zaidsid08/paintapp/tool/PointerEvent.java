// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.tool;

import io.github.zaidsid08.paintapp.model.Vec2;

/**
 * A mouse event already converted to document coordinates. Tools see this
 * rather than a JavaFX MouseEvent, which keeps them testable without a UI.
 */
public record PointerEvent(Vec2 position, boolean shift, int clickCount) {

    public static PointerEvent at(double x, double y) {
        return new PointerEvent(new Vec2(x, y), false, 1);
    }

    public PointerEvent withShift() {
        return new PointerEvent(position, true, clickCount);
    }

    public PointerEvent withClicks(int n) {
        return new PointerEvent(position, shift, n);
    }
}
