// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.command;

import io.github.zaidsid08.paintapp.model.Document;
import io.github.zaidsid08.paintapp.model.Element;
import java.util.List;

/**
 * Swaps elements for new versions with the same ids. Moving and restyling
 * are both expressed this way.
 */
public final class ReplaceElements implements Command {

    private final List<Element> before;
    private final List<Element> after;
    private final String label;

    public ReplaceElements(List<Element> before, List<Element> after, String label) {
        if (before.size() != after.size() || before.isEmpty()) {
            throw new IllegalArgumentException("before/after must be non-empty and the same size");
        }
        for (int i = 0; i < before.size(); i++) {
            if (before.get(i).id() != after.get(i).id()) {
                throw new IllegalArgumentException("before/after ids must line up");
            }
        }
        this.before = List.copyOf(before);
        this.after = List.copyOf(after);
        this.label = label;
    }

    @Override
    public void apply(Document doc) {
        after.forEach(doc::replace);
    }

    @Override
    public void revert(Document doc) {
        before.forEach(doc::replace);
    }

    @Override
    public String label() {
        return label;
    }
}
