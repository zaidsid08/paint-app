// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The drawing: an ordered list of elements, back to front. All edits go
 * through here so listeners (the canvas, the "unsaved changes" flag) hear
 * about every change.
 */
public final class Document {

    /** Notified after every structural or content change. */
    @FunctionalInterface
    public interface Listener {
        void documentChanged(Document doc);
    }

    private final List<Element> elements = new ArrayList<>();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private long nextId = 1;

    public long newId() {
        return nextId++;
    }

    public List<Element> elements() {
        return Collections.unmodifiableList(elements);
    }

    public int size() {
        return elements.size();
    }

    public void add(Element e) {
        insert(elements.size(), e);
    }

    public void insert(int index, Element e) {
        if (indexOf(e.id()) >= 0) {
            throw new IllegalArgumentException("duplicate element id " + e.id());
        }
        elements.add(index, e);
        nextId = Math.max(nextId, e.id() + 1);
        fire();
    }

    /** Removes the element and returns the index it occupied. */
    public int remove(long id) {
        int i = requireIndex(id);
        elements.remove(i);
        fire();
        return i;
    }

    /** Swaps in a new version of an element that has the same id. */
    public void replace(Element e) {
        elements.set(requireIndex(e.id()), e);
        fire();
    }

    /** Moves an element to a new z-position (0 = back). */
    public void move(long id, int newIndex) {
        Element e = elements.remove(requireIndex(id));
        elements.add(Math.max(0, Math.min(newIndex, elements.size())), e);
        fire();
    }

    public void clear() {
        elements.clear();
        nextId = 1;
        fire();
    }

    public int indexOf(long id) {
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i).id() == id) {
                return i;
            }
        }
        return -1;
    }

    public Optional<Element> find(long id) {
        int i = indexOf(id);
        return i < 0 ? Optional.empty() : Optional.of(elements.get(i));
    }

    /** The front-most element under p, if any. */
    public Optional<Element> topmostAt(Vec2 p, double tolerance) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            if (elements.get(i).hit(p, tolerance)) {
                return Optional.of(elements.get(i));
            }
        }
        return Optional.empty();
    }

    /** Elements lying entirely inside the region (marquee selection). */
    public List<Element> within(Bounds region) {
        return elements.stream().filter(e -> region.contains(e.bounds())).toList();
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    private int requireIndex(long id) {
        int i = indexOf(id);
        if (i < 0) {
            throw new IllegalArgumentException("no element with id " + id);
        }
        return i;
    }

    private void fire() {
        for (Listener l : listeners) {
            l.documentChanged(this);
        }
    }
}
