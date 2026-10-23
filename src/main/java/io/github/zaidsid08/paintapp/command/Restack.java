// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.command;

import io.github.zaidsid08.paintapp.model.Document;
import io.github.zaidsid08.paintapp.model.Element;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/** Changes the z-order of a group of elements. */
public final class Restack implements Command {

    public enum Direction {
        FORWARD("Bring forward"),
        BACKWARD("Send backward"),
        TO_FRONT("Bring to front"),
        TO_BACK("Send to back");

        final String label;

        Direction(String label) {
            this.label = label;
        }
    }

    private final Set<Long> ids;
    private final Direction direction;
    private List<Long> previousOrder = List.of();

    public Restack(Collection<Long> ids, Direction direction) {
        this.ids = Set.copyOf(ids);
        this.direction = direction;
    }

    @Override
    public void apply(Document doc) {
        previousOrder = order(doc);
        List<Long> next = new ArrayList<>(previousOrder);
        switch (direction) {
            case TO_FRONT -> {
                next.removeIf(ids::contains);
                previousOrder.stream().filter(ids::contains).forEach(next::add);
            }
            case TO_BACK -> {
                next.removeIf(ids::contains);
                next.addAll(0, previousOrder.stream().filter(ids::contains).toList());
            }
            // One step: walk away from the destination end so a selected run
            // moves as a block instead of leapfrogging itself.
            case FORWARD -> {
                for (int i = next.size() - 2; i >= 0; i--) {
                    if (ids.contains(next.get(i)) && !ids.contains(next.get(i + 1))) {
                        next.add(i + 1, next.remove(i));
                    }
                }
            }
            case BACKWARD -> {
                for (int i = 1; i < next.size(); i++) {
                    if (ids.contains(next.get(i)) && !ids.contains(next.get(i - 1))) {
                        next.add(i - 1, next.remove(i));
                    }
                }
            }
        }
        reorder(doc, next);
    }

    @Override
    public void revert(Document doc) {
        reorder(doc, previousOrder);
    }

    @Override
    public String label() {
        return direction.label;
    }

    private static List<Long> order(Document doc) {
        return doc.elements().stream().map(Element::id).toList();
    }

    private static void reorder(Document doc, List<Long> target) {
        for (int i = 0; i < target.size(); i++) {
            if (doc.indexOf(target.get(i)) != i) {
                doc.move(target.get(i), i);
            }
        }
    }
}
