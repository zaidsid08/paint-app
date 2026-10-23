// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.zaidsid08.paintapp.editor.Editor;
import io.github.zaidsid08.paintapp.model.Bounds;
import io.github.zaidsid08.paintapp.model.EllipseShape;
import io.github.zaidsid08.paintapp.model.FreehandStroke;
import io.github.zaidsid08.paintapp.model.LineSegment;
import io.github.zaidsid08.paintapp.model.PolyShape;
import io.github.zaidsid08.paintapp.model.RectShape;
import io.github.zaidsid08.paintapp.model.Vec2;
import org.junit.jupiter.api.Test;

/**
 * Tools are driven with synthetic pointer events against a real Editor, the
 * same path the UI takes, minus JavaFX.
 */
class ToolsTest {

    private final Editor editor = new Editor();

    private void drag(String tool, double x0, double y0, double x1, double y1, boolean shift) {
        editor.selectTool(tool);
        PointerEvent a = PointerEvent.at(x0, y0), b = PointerEvent.at(x1, y1);
        if (shift) {
            a = a.withShift();
            b = b.withShift();
        }
        editor.tool().pressed(a);
        editor.tool().dragged(b);
        editor.tool().released(b);
    }

    private void click(double x, double y) {
        editor.tool().pressed(PointerEvent.at(x, y));
        editor.tool().released(PointerEvent.at(x, y));
    }

    @Test
    void rectangleToolDrawsInAnyDragDirection() {
        drag("Rectangle", 50, 40, 10, 20, false);
        RectShape r = assertInstanceOf(RectShape.class, editor.document().elements().get(0));
        assertEquals(new Bounds(10, 20, 50, 40), r.box());
        assertTrue(editor.preview().isEmpty());
    }

    @Test
    void shiftConstrainsRectToSquareAndEllipseToCircle() {
        drag("Rectangle", 0, 0, 30, -10, true);
        RectShape sq = (RectShape) editor.document().elements().get(0);
        assertEquals(new Bounds(0, -30, 30, 0), sq.box());

        drag("Ellipse", 0, 0, -8, 20, true);
        EllipseShape c = (EllipseShape) editor.document().elements().get(1);
        assertEquals(c.box().width(), c.box().height());
    }

    @Test
    void shiftSnapsLinesTo45Degrees() {
        drag("Line", 0, 0, 100, 10, true);
        LineSegment l = (LineSegment) editor.document().elements().get(0);
        assertEquals(0, l.end().y(), 1e-9);
        drag("Line", 0, 0, 50, 48, true);
        LineSegment diag = (LineSegment) editor.document().elements().get(1);
        assertEquals(diag.end().x(), diag.end().y(), 1e-9);
    }

    @Test
    void accidentalClicksDontCreateShapes() {
        drag("Rectangle", 10, 10, 10.3, 10.2, false);
        assertEquals(0, editor.document().size());
        assertFalse(editor.history().canUndo());
    }

    @Test
    void penSimplifiesAndIsOneUndoStep() {
        editor.selectTool("Pen");
        editor.tool().pressed(PointerEvent.at(0, 0));
        for (int i = 1; i <= 200; i++) {
            editor.tool().dragged(PointerEvent.at(i, 0)); // a straight line
        }
        editor.tool().released(PointerEvent.at(200, 0));
        FreehandStroke s = (FreehandStroke) editor.document().elements().get(0);
        assertEquals(2, s.points().size());
        editor.undo();
        assertEquals(0, editor.document().size());
    }

    @Test
    void polylineClosesWhenClickingTheFirstPoint() {
        editor.selectTool("Polyline");
        click(0, 0);
        click(100, 0);
        click(50, 80);
        click(1, 1); // within the close radius of (0,0)
        PolyShape p = (PolyShape) editor.document().elements().get(0);
        assertTrue(p.closed());
        assertEquals(3, p.vertices().size());
    }

    @Test
    void polylineFinishesOpenOnEnterAndCancelsOnEscape() {
        editor.selectTool("Polyline");
        click(0, 0);
        click(10, 10);
        editor.tool().key(Key.ENTER, false);
        assertFalse(((PolyShape) editor.document().elements().get(0)).closed());

        click(50, 50);
        click(60, 60);
        editor.tool().key(Key.ESCAPE, false);
        assertEquals(1, editor.document().size());
        assertTrue(editor.preview().isEmpty());
    }

    @Test
    void switchingToolsCommitsAnUnfinishedPolyline() {
        editor.selectTool("Polyline");
        click(0, 0);
        click(30, 0);
        editor.selectTool("Pen");
        assertEquals(1, editor.document().size());
    }

    @Test
    void eraserRemovesEverythingTouchedInOneUndoableStep() {
        drag("Line", 0, 0, 100, 0, false);
        drag("Line", 0, 50, 100, 50, false);
        drag("Line", 0, 200, 100, 200, false);

        editor.selectTool("Eraser");
        editor.tool().pressed(PointerEvent.at(50, -10));
        editor.tool().dragged(PointerEvent.at(50, 0));
        editor.tool().dragged(PointerEvent.at(50, 50));
        assertEquals(2, editor.highlighted().size());
        editor.tool().released(PointerEvent.at(50, 60));

        assertEquals(1, editor.document().size());
        assertTrue(editor.highlighted().isEmpty());
        editor.undo();
        assertEquals(3, editor.document().size());
    }

    @Test
    void selectClickShiftClickAndMarquee() {
        drag("Rectangle", 0, 0, 10, 10, false);
        drag("Rectangle", 100, 0, 110, 10, false);
        drag("Rectangle", 300, 300, 310, 310, false);
        editor.selectTool("Select");

        click(0, 5);
        assertEquals(1, editor.selection().size());
        editor.tool().pressed(PointerEvent.at(100, 5).withShift());
        editor.tool().released(PointerEvent.at(100, 5).withShift());
        assertEquals(2, editor.selection().size());

        click(200, 200); // empty space clears
        assertTrue(editor.selection().isEmpty());

        editor.tool().pressed(PointerEvent.at(-5, -5));
        editor.tool().dragged(PointerEvent.at(120, 20));
        assertTrue(editor.marquee().isPresent());
        editor.tool().released(PointerEvent.at(120, 20));
        assertEquals(2, editor.selection().size());
        assertTrue(editor.marquee().isEmpty());
    }

    @Test
    void draggingTheSelectionMovesItAsOneUndoStep() {
        drag("Rectangle", 0, 0, 10, 10, false);
        drag("Rectangle", 20, 0, 30, 10, false);
        editor.selectAll();
        editor.selectTool("Select");

        editor.tool().pressed(PointerEvent.at(0, 5));
        for (int i = 1; i <= 10; i++) {
            editor.tool().dragged(PointerEvent.at(i * 5, 5));
        }
        editor.tool().released(PointerEvent.at(50, 5));

        assertEquals(50, editor.document().elements().get(0).bounds().minX());
        assertEquals(70, editor.document().elements().get(1).bounds().minX());
        assertEquals("Move 2 elements", editor.history().undoLabel().orElseThrow());
        editor.undo();
        assertEquals(0, editor.document().elements().get(0).bounds().minX());
    }

    @Test
    void arrowKeysNudgeTheSelection() {
        drag("Rectangle", 0, 0, 10, 10, false);
        editor.selectAll();
        editor.selectTool("Select");
        editor.tool().key(Key.RIGHT, false);
        editor.tool().key(Key.DOWN, true);
        assertEquals(new Vec2(1, 10), new Vec2(editor.document().elements().get(0).bounds().minX(),
                editor.document().elements().get(0).bounds().minY()));
    }
}
