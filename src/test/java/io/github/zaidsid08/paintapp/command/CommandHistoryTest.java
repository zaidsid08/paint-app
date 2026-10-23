// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.zaidsid08.paintapp.model.Bounds;
import io.github.zaidsid08.paintapp.model.Document;
import io.github.zaidsid08.paintapp.model.Element;
import io.github.zaidsid08.paintapp.model.RectShape;
import io.github.zaidsid08.paintapp.model.Style;
import io.github.zaidsid08.paintapp.model.Vec2;
import java.util.List;
import org.junit.jupiter.api.Test;

class CommandHistoryTest {

    private final Document doc = new Document();
    private final CommandHistory history = new CommandHistory(doc, 100);

    private static RectShape rect(long id) {
        return new RectShape(id, new Bounds(0, 0, 10, 10), Style.DEFAULT);
    }

    private List<Long> ids() {
        return doc.elements().stream().map(Element::id).toList();
    }

    @Test
    void undoAndRedoWalkBothWays() {
        history.execute(AddElements.one(rect(1)));
        history.execute(AddElements.one(rect(2)));
        history.undo();
        assertEquals(List.of(1L), ids());
        history.undo();
        assertEquals(List.of(), ids());
        history.redo();
        history.redo();
        assertEquals(List.of(1L, 2L), ids());
    }

    @Test
    void newCommandClearsRedo() {
        history.execute(AddElements.one(rect(1)));
        history.undo();
        assertTrue(history.canRedo());
        history.execute(AddElements.one(rect(2)));
        assertFalse(history.canRedo());
    }

    @Test
    void labelsDescribeTheNextStep() {
        history.execute(AddElements.one(rect(1)));
        assertEquals("Draw rectangle", history.undoLabel().orElseThrow());
        history.undo();
        assertEquals("Draw rectangle", history.redoLabel().orElseThrow());
    }

    @Test
    void oldestCommandsAreDroppedPastTheLimit() {
        CommandHistory small = new CommandHistory(doc, 2);
        for (long i = 1; i <= 3; i++) {
            small.execute(AddElements.one(rect(i)));
        }
        small.undo();
        small.undo();
        assertFalse(small.canUndo());
        assertEquals(List.of(1L), ids()); // the first add can no longer be undone
    }

    @Test
    void dirtyTrackingFollowsTheSavedPoint() {
        assertFalse(history.isDirty());
        history.execute(AddElements.one(rect(1)));
        assertTrue(history.isDirty());
        history.markClean();
        assertFalse(history.isDirty());
        history.undo();
        assertTrue(history.isDirty()); // differs from what's on disk
        history.redo();
        assertFalse(history.isDirty()); // back to the saved state
    }

    @Test
    void removeRestoresOriginalZOrder() {
        for (long i = 1; i <= 5; i++) {
            doc.add(rect(i));
        }
        history.execute(new RemoveElements(List.of(4L, 2L)));
        assertEquals(List.of(1L, 3L, 5L), ids());
        history.undo();
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), ids());
        history.redo();
        assertEquals(List.of(1L, 3L, 5L), ids());
    }

    @Test
    void replaceSwapsVersionsBothWays() {
        doc.add(rect(1));
        RectShape before = rect(1);
        RectShape after = before.translate(new Vec2(5, 0));
        history.execute(new ReplaceElements(List.of(before), List.of(after), "Move"));
        assertEquals(after, doc.find(1).orElseThrow());
        history.undo();
        assertEquals(before, doc.find(1).orElseThrow());
    }

    @Test
    void restackMovesSelectedBlockOneStepAndUndoes() {
        for (long i = 1; i <= 5; i++) {
            doc.add(rect(i));
        }
        history.execute(new Restack(List.of(2L, 3L), Restack.Direction.FORWARD));
        assertEquals(List.of(1L, 4L, 2L, 3L, 5L), ids());
        history.execute(new Restack(List.of(2L, 3L), Restack.Direction.TO_BACK));
        assertEquals(List.of(2L, 3L, 1L, 4L, 5L), ids());
        history.execute(new Restack(List.of(1L), Restack.Direction.TO_FRONT));
        assertEquals(List.of(2L, 3L, 4L, 5L, 1L), ids());
        history.execute(new Restack(List.of(4L), Restack.Direction.BACKWARD));
        assertEquals(List.of(2L, 4L, 3L, 5L, 1L), ids());
        history.undo();
        history.undo();
        history.undo();
        history.undo();
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), ids());
    }
}
