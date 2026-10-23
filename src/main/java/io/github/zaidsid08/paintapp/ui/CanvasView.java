// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.ui;

import io.github.zaidsid08.paintapp.editor.Editor;
import io.github.zaidsid08.paintapp.model.Vec2;
import io.github.zaidsid08.paintapp.tool.PointerEvent;
import java.util.function.Consumer;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Pane;

/**
 * The drawing surface. Converts mouse input to document coordinates and
 * hands it to the active tool; handles zoom (Ctrl/Cmd + scroll, pinch) and
 * pan (scroll, middle-drag, or Space + drag) itself.
 */
final class CanvasView extends Pane {

    private static final double ZOOM_STEP = 1.1;

    private final Canvas canvas = new Canvas();
    private final Editor editor;
    private Consumer<Vec2> pointerListener = p -> { };
    private boolean spaceHeld;
    private boolean panning;
    private double lastX, lastY;

    CanvasView(Editor editor) {
        this.editor = editor;
        getChildren().add(canvas);
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        canvas.widthProperty().addListener((o, a, b) -> repaint());
        canvas.heightProperty().addListener((o, a, b) -> repaint());
        setMinSize(200, 200);
        setCursor(Cursor.CROSSHAIR);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onPressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onDragged);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onReleased);
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, this::onMoved);
        canvas.addEventHandler(ScrollEvent.SCROLL, this::onScroll);
        canvas.addEventHandler(ZoomEvent.ZOOM, e -> {
            editor.viewport().zoomAt(new Vec2(e.getX(), e.getY()), e.getZoomFactor());
            repaint();
        });
    }

    void setPointerListener(Consumer<Vec2> listener) {
        this.pointerListener = listener;
    }

    void setSpaceHeld(boolean held) {
        spaceHeld = held;
        setCursor(held ? Cursor.OPEN_HAND : Cursor.CROSSHAIR);
    }

    void repaint() {
        Renderer.paintEditor(canvas.getGraphicsContext2D(), editor, canvas.getWidth(), canvas.getHeight());
    }

    private PointerEvent toPointer(MouseEvent e) {
        Vec2 doc = editor.viewport().toDocument(new Vec2(e.getX(), e.getY()));
        return new PointerEvent(doc, e.isShiftDown(), e.getClickCount());
    }

    private void onPressed(MouseEvent e) {
        requestFocus();
        if (e.getButton() == MouseButton.MIDDLE || (spaceHeld && e.getButton() == MouseButton.PRIMARY)) {
            panning = true;
            lastX = e.getX();
            lastY = e.getY();
            setCursor(Cursor.CLOSED_HAND);
        } else if (e.getButton() == MouseButton.PRIMARY) {
            editor.tool().pressed(toPointer(e));
        }
    }

    private void onDragged(MouseEvent e) {
        if (panning) {
            editor.viewport().panBy(e.getX() - lastX, e.getY() - lastY);
            lastX = e.getX();
            lastY = e.getY();
            repaint();
        } else if (e.isPrimaryButtonDown()) {
            editor.tool().dragged(toPointer(e));
        }
        pointerListener.accept(toPointer(e).position());
    }

    private void onReleased(MouseEvent e) {
        if (panning) {
            panning = false;
            setCursor(spaceHeld ? Cursor.OPEN_HAND : Cursor.CROSSHAIR);
        } else if (e.getButton() == MouseButton.PRIMARY) {
            editor.tool().released(toPointer(e));
        }
    }

    private void onMoved(MouseEvent e) {
        PointerEvent p = toPointer(e);
        editor.tool().moved(p);
        pointerListener.accept(p.position());
    }

    private void onScroll(ScrollEvent e) {
        if (e.isControlDown() || e.isShortcutDown()) {
            double factor = e.getDeltaY() > 0 ? ZOOM_STEP : 1 / ZOOM_STEP;
            editor.viewport().zoomAt(new Vec2(e.getX(), e.getY()), factor);
        } else {
            editor.viewport().panBy(e.getDeltaX(), e.getDeltaY());
        }
        repaint();
        e.consume();
    }
}
