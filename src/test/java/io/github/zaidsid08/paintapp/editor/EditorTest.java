// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.zaidsid08.paintapp.io.FormatException;
import io.github.zaidsid08.paintapp.model.Element;
import io.github.zaidsid08.paintapp.model.Rgba;
import io.github.zaidsid08.paintapp.model.Style;
import io.github.zaidsid08.paintapp.model.Vec2;
import io.github.zaidsid08.paintapp.tool.PointerEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EditorTest {

    private final Editor editor = new Editor();

    private void rect(double x, double y) {
        editor.selectTool("Rectangle");
        editor.tool().pressed(PointerEvent.at(x, y));
        editor.tool().released(PointerEvent.at(x + 20, y + 20));
    }

    @Test
    void styleChangeAppliesToSelectionAsOneStep() {
        rect(0, 0);
        rect(50, 0);
        editor.selectAll();
        Style red = new Style(Rgba.fromHex("#ff0000"), null, 6);
        editor.setStyle(red);
        assertTrue(editor.document().elements().stream().allMatch(e -> e.style().equals(red)));
        assertEquals("Change style", editor.history().undoLabel().orElseThrow());
        editor.undo();
        assertTrue(editor.document().elements().stream().allMatch(e -> e.style().equals(Style.DEFAULT)));
    }

    @Test
    void settingTheSameStyleAddsNoHistory() {
        rect(0, 0);
        editor.selectAll();
        editor.setStyle(Style.DEFAULT);
        assertEquals("Draw rectangle", editor.history().undoLabel().orElseThrow());
    }

    @Test
    void duplicateCreatesOffsetCopiesWithFreshIds() {
        rect(0, 0);
        editor.selectAll();
        long original = editor.document().elements().get(0).id();
        editor.duplicateSelection();
        assertEquals(2, editor.document().size());
        Element copy = editor.document().elements().get(1);
        assertNotEquals(original, copy.id());
        assertEquals(12, copy.bounds().minX());
        assertEquals(Set.of(copy.id()), editor.selection().ids());
    }

    @Test
    void undoDropsSelectionOfElementsThatNoLongerExist() {
        rect(0, 0);
        editor.selectAll();
        editor.undo();
        assertTrue(editor.selection().isEmpty());
    }

    @Test
    void deleteSelection() {
        rect(0, 0);
        rect(50, 0);
        editor.selection().select(editor.document().elements().get(0).id());
        editor.deleteSelection();
        assertEquals(1, editor.document().size());
        assertTrue(editor.selection().isEmpty());
    }

    @Test
    void saveOpenRoundTripAndDirtyFlag(@TempDir Path dir) throws Exception {
        rect(0, 0);
        rect(50, 50);
        assertTrue(editor.isDirty());
        Path file = dir.resolve("drawing.paint");
        editor.save(file);
        assertFalse(editor.isDirty());

        Editor other = new Editor();
        other.open(file);
        assertEquals(editor.document().elements(), other.document().elements());
        assertFalse(other.isDirty());
        assertFalse(other.history().canUndo());
        assertEquals(file, other.file().orElseThrow());
    }

    @Test
    void openingABadFileKeepsTheCurrentDrawing(@TempDir Path dir) throws Exception {
        rect(0, 0);
        Path bad = dir.resolve("bad.paint");
        Files.writeString(bad, "paintapp 1\nrect 1 stroke=#000000 pts=oops\n");
        assertThrows(FormatException.class, () -> editor.open(bad));
        assertEquals(1, editor.document().size());
    }

    @Test
    void hitToleranceIsConstantInScreenPixels() {
        double atOne = editor.hitTolerance();
        editor.viewport().zoomAt(Vec2.ZERO, 2);
        assertEquals(atOne / 2, editor.hitTolerance(), 1e-9);
    }

    @Test
    void unknownToolNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> editor.selectTool("Spray can"));
    }
}
