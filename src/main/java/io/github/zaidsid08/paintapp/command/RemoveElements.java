// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.command;

import io.github.zaidsid08.paintapp.model.Document;
import io.github.zaidsid08.paintapp.model.Element;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** Deletes elements, remembering their z-positions so undo puts them back exactly. */
public final class RemoveElements implements Command {

    private record Slot(int index, Element element) {
    }

    private final List<Long> ids;
    private final List<Slot> removed = new ArrayList<>();

    public RemoveElements(Collection<Long> ids) {
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("nothing to remove");
        }
        this.ids = List.copyOf(ids);
    }

    @Override
    public void apply(Document doc) {
        removed.clear();
        for (long id : ids) {
            int index = doc.indexOf(id);
            if (index >= 0) {
                removed.add(new Slot(index, doc.elements().get(index)));
            }
        }
        // Remove from the top down so earlier indices stay valid.
        removed.sort(Comparator.comparingInt(Slot::index).reversed());
        removed.forEach(s -> doc.remove(s.element().id()));
    }

    @Override
    public void revert(Document doc) {
        // Re-insert bottom up: each original index is correct once everything
        // below it is back in place.
        for (int i = removed.size() - 1; i >= 0; i--) {
            Slot s = removed.get(i);
            doc.insert(s.index(), s.element());
        }
    }

    @Override
    public String label() {
        return ids.size() == 1 ? "Delete" : "Delete " + ids.size() + " elements";
    }
}
