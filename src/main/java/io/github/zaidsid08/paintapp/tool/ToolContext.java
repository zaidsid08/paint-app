// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.tool;

import io.github.zaidsid08.paintapp.command.CommandHistory;
import io.github.zaidsid08.paintapp.model.Bounds;
import io.github.zaidsid08.paintapp.model.Document;
import io.github.zaidsid08.paintapp.model.Element;
import io.github.zaidsid08.paintapp.model.Selection;
import io.github.zaidsid08.paintapp.model.Style;
import java.util.Set;

/** What a tool is allowed to see and touch. Implemented by the editor. */
public interface ToolContext {

    Document document();

    CommandHistory history();

    Selection selection();

    /** Style that newly drawn elements get. */
    Style currentStyle();

    /** How close (in document units) a click must be to count as a hit at the current zoom. */
    double hitTolerance();

    /** An in-progress element drawn on top of the document, or null for none. */
    void setPreview(Element preview);

    /** The rubber-band selection rectangle, or null for none. */
    void setMarquee(Bounds marquee);

    /** Elements to draw faded, e.g. the ones an eraser drag is about to delete. */
    void setHighlighted(Set<Long> ids);

    void requestRepaint();
}
