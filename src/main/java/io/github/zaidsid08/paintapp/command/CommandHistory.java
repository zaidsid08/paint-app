// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.command;

import io.github.zaidsid08.paintapp.model.Document;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Undo/redo stacks for one document. Also tracks the "clean" point, the
 * command that was on top when the file was last saved, so the window can
 * show unsaved changes without a separate dirty flag going out of sync.
 */
public final class CommandHistory {

    @FunctionalInterface
    public interface Listener {
        void historyChanged(CommandHistory h);
    }

    private static final Object EMPTY = new Object();

    private final Document doc;
    private final int limit;
    private final Deque<Command> undo = new ArrayDeque<>();
    private final Deque<Command> redo = new ArrayDeque<>();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private Object cleanMarker = EMPTY;

    public CommandHistory(Document doc, int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be positive");
        }
        this.doc = doc;
        this.limit = limit;
    }

    public void execute(Command c) {
        c.apply(doc);
        undo.push(c);
        redo.clear();
        if (undo.size() > limit) {
            Command dropped = undo.removeLast();
            if (dropped == cleanMarker) {
                cleanMarker = null; // the saved state can no longer be reached
            }
        }
        fire();
    }

    public boolean canUndo() {
        return !undo.isEmpty();
    }

    public boolean canRedo() {
        return !redo.isEmpty();
    }

    public Optional<String> undoLabel() {
        return Optional.ofNullable(undo.peek()).map(Command::label);
    }

    public Optional<String> redoLabel() {
        return Optional.ofNullable(redo.peek()).map(Command::label);
    }

    public void undo() {
        if (undo.isEmpty()) {
            return;
        }
        Command c = undo.pop();
        c.revert(doc);
        redo.push(c);
        fire();
    }

    public void redo() {
        if (redo.isEmpty()) {
            return;
        }
        Command c = redo.pop();
        c.apply(doc);
        undo.push(c);
        fire();
    }

    /** Forget all history, e.g. after opening a different file. */
    public void reset() {
        undo.clear();
        redo.clear();
        cleanMarker = EMPTY;
        fire();
    }

    public void markClean() {
        cleanMarker = undo.isEmpty() ? EMPTY : undo.peek();
        fire();
    }

    public boolean isDirty() {
        Object top = undo.isEmpty() ? EMPTY : undo.peek();
        return top != cleanMarker;
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }

    private void fire() {
        for (Listener l : listeners) {
            l.historyChanged(this);
        }
    }
}
