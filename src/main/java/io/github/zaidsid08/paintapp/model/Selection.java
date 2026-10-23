// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/** The set of selected element ids, in the order they were selected. */
public final class Selection {

    @FunctionalInterface
    public interface Listener {
        void selectionChanged(Selection s);
    }

    private final Set<Long> ids = new LinkedHashSet<>();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    public Set<Long> ids() {
        return Set.copyOf(ids);
    }

    public boolean contains(long id) {
        return ids.contains(id);
    }

    public boolean isEmpty() {
        return ids.isEmpty();
    }

    public int size() {
        return ids.size();
    }

    public void set(Collection<Long> newIds) {
        if (ids.equals(new LinkedHashSet<>(newIds))) {
            return;
        }
        ids.clear();
        ids.addAll(newIds);
        fire();
    }

    public void select(long id) {
        set(List.of(id));
    }

    public void toggle(long id) {
        if (!ids.remove(id)) {
            ids.add(id);
        }
        fire();
    }

    public void clear() {
        if (!ids.isEmpty()) {
            ids.clear();
            fire();
        }
    }

    /** Drops ids whose elements no longer exist (e.g. after an undo). */
    public void retainExisting(Document doc) {
        if (ids.removeIf(id -> doc.indexOf(id) < 0)) {
            fire();
        }
    }

    /** Selected elements, back to front. */
    public List<Element> elementsIn(Document doc) {
        return doc.elements().stream().filter(e -> ids.contains(e.id())).toList();
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }

    private void fire() {
        for (Listener l : listeners) {
            l.selectionChanged(this);
        }
    }
}
