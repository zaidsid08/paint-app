// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.command;

import io.github.zaidsid08.paintapp.model.Document;

/**
 * A reversible edit. Every change the user makes goes through a command so it
 * can be undone. {@code apply} may be called again after {@code revert} (redo),
 * so implementations must not rely on one-shot state.
 */
public interface Command {

    void apply(Document doc);

    void revert(Document doc);

    /** Short description for the Edit menu, e.g. "Move 3 elements". */
    String label();
}
