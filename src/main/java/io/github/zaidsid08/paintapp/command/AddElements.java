// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.command;

import io.github.zaidsid08.paintapp.model.Document;
import io.github.zaidsid08.paintapp.model.Element;
import java.util.List;

/** Appends elements on top of the drawing. */
public final class AddElements implements Command {

    private final List<Element> added;
    private final String label;

    public AddElements(List<Element> added, String label) {
        if (added.isEmpty()) {
            throw new IllegalArgumentException("nothing to add");
        }
        this.added = List.copyOf(added);
        this.label = label;
    }

    public static AddElements one(Element e) {
        return new AddElements(List.of(e), "Draw " + Labels.kindOf(e));
    }

    @Override
    public void apply(Document doc) {
        added.forEach(doc::add);
    }

    @Override
    public void revert(Document doc) {
        for (int i = added.size() - 1; i >= 0; i--) {
            doc.remove(added.get(i).id());
        }
    }

    @Override
    public String label() {
        return label;
    }
}
