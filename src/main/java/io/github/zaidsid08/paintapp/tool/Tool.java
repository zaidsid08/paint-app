// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.tool;

/**
 * One mode of interaction with the canvas (select, pen, rectangle, ...).
 * The canvas forwards pointer input to whichever tool is active. Tools turn
 * gestures into commands; they never edit the document behind the history's
 * back, except for live previews that they put back before committing.
 */
public interface Tool {

    String name();

    /** Short hint for the status bar. */
    String hint();

    default void pressed(PointerEvent e) {
    }

    default void dragged(PointerEvent e) {
    }

    default void released(PointerEvent e) {
    }

    /** Pointer moved with no button held. */
    default void moved(PointerEvent e) {
    }

    default void key(Key key, boolean shift) {
    }

    /** Called when the user switches to another tool, to finish or abandon work in progress. */
    default void deactivate() {
    }
}
