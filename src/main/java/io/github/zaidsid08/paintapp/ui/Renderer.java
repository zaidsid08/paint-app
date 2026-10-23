// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.ui;

import io.github.zaidsid08.paintapp.editor.Editor;
import io.github.zaidsid08.paintapp.editor.Viewport;
import io.github.zaidsid08.paintapp.model.Bounds;
import io.github.zaidsid08.paintapp.model.Element;
import io.github.zaidsid08.paintapp.model.EllipseShape;
import io.github.zaidsid08.paintapp.model.FreehandStroke;
import io.github.zaidsid08.paintapp.model.LineSegment;
import io.github.zaidsid08.paintapp.model.PolyShape;
import io.github.zaidsid08.paintapp.model.RectShape;
import io.github.zaidsid08.paintapp.model.Smoothing;
import io.github.zaidsid08.paintapp.model.Style;
import io.github.zaidsid08.paintapp.model.Vec2;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Affine;

/** Paints the editor state onto a canvas. Stateless: everything comes from the Editor. */
final class Renderer {

    private static final Color BACKGROUND = Color.web("#f7f6f3");
    private static final Color GRID_DOT = Color.web("#d9d6cf");
    private static final Color ACCENT = Color.web("#2f6fed");
    private static final double GRID_STEP = 20;

    private Renderer() {
    }

    /** Full editor view: background, grid, elements, preview and selection overlays. */
    static void paintEditor(GraphicsContext gc, Editor editor, double width, double height) {
        Viewport vp = editor.viewport();
        gc.setTransform(new Affine());
        gc.setFill(BACKGROUND);
        gc.fillRect(0, 0, width, height);
        paintGrid(gc, vp, width, height);

        gc.setTransform(new Affine(vp.scale(), 0, vp.offsetX(), 0, vp.scale(), vp.offsetY()));
        for (Element e : editor.document().elements()) {
            gc.setGlobalAlpha(editor.highlighted().contains(e.id()) ? 0.25 : 1);
            paintElement(gc, e);
        }
        gc.setGlobalAlpha(1);
        editor.preview().ifPresent(p -> paintElement(gc, p));

        double px = 1 / vp.scale(); // one screen pixel in document units
        gc.setStroke(ACCENT);
        gc.setLineWidth(px);
        gc.setLineDashes(4 * px, 3 * px);
        for (Element e : editor.selection().elementsIn(editor.document())) {
            Bounds b = e.paintedBounds().inflate(3 * px);
            gc.strokeRect(b.minX(), b.minY(), b.width(), b.height());
        }
        gc.setLineDashes((double[]) null);

        editor.marquee().ifPresent(m -> {
            gc.setFill(ACCENT.deriveColor(0, 1, 1, 0.12));
            gc.fillRect(m.minX(), m.minY(), m.width(), m.height());
            gc.setStroke(ACCENT);
            gc.strokeRect(m.minX(), m.minY(), m.width(), m.height());
        });
        gc.setTransform(new Affine());
    }

    /** Just the drawing, for PNG export. The transform should already map the content. */
    static void paintElements(GraphicsContext gc, List<Element> elements) {
        elements.forEach(e -> paintElement(gc, e));
    }

    private static void paintGrid(GraphicsContext gc, Viewport vp, double width, double height) {
        double step = GRID_STEP * vp.scale();
        if (step < 8) {
            return; // too dense to be useful when zoomed far out
        }
        gc.setFill(GRID_DOT);
        double startX = vp.offsetX() % step, startY = vp.offsetY() % step;
        for (double x = startX; x < width; x += step) {
            for (double y = startY; y < height; y += step) {
                gc.fillRect(x - 0.75, y - 0.75, 1.5, 1.5);
            }
        }
    }

    static void paintElement(GraphicsContext gc, Element e) {
        Style s = e.style();
        gc.setStroke(FxColors.toFx(s.stroke()));
        gc.setLineWidth(s.strokeWidth());
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        Color fill = s.fill() == null ? null : FxColors.toFx(s.fill());

        switch (e) {
            case FreehandStroke st -> paintFreehand(gc, st);
            case LineSegment l -> gc.strokeLine(l.start().x(), l.start().y(), l.end().x(), l.end().y());
            case RectShape r -> {
                Bounds b = r.box();
                if (fill != null) {
                    gc.setFill(fill);
                    gc.fillRect(b.minX(), b.minY(), b.width(), b.height());
                }
                gc.strokeRect(b.minX(), b.minY(), b.width(), b.height());
            }
            case EllipseShape el -> {
                Bounds b = el.box();
                if (fill != null) {
                    gc.setFill(fill);
                    gc.fillOval(b.minX(), b.minY(), b.width(), b.height());
                }
                gc.strokeOval(b.minX(), b.minY(), b.width(), b.height());
            }
            case PolyShape p -> paintPoly(gc, p, fill);
        }
    }

    private static void paintFreehand(GraphicsContext gc, FreehandStroke s) {
        List<Vec2> pts = s.points();
        if (pts.size() == 1) {
            double r = s.style().strokeWidth() / 2;
            gc.setFill(FxColors.toFx(s.style().stroke()));
            gc.fillOval(pts.get(0).x() - r, pts.get(0).y() - r, 2 * r, 2 * r);
            return;
        }
        gc.beginPath();
        gc.moveTo(pts.get(0).x(), pts.get(0).y());
        for (Smoothing.Cubic c : Smoothing.catmullRom(pts)) {
            gc.bezierCurveTo(c.c1().x(), c.c1().y(), c.c2().x(), c.c2().y(), c.to().x(), c.to().y());
        }
        gc.stroke();
    }

    private static void paintPoly(GraphicsContext gc, PolyShape p, Color fill) {
        int n = p.vertices().size();
        double[] xs = new double[n], ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = p.vertices().get(i).x();
            ys[i] = p.vertices().get(i).y();
        }
        if (p.closed()) {
            if (fill != null) {
                gc.setFill(fill);
                gc.fillPolygon(xs, ys, n);
            }
            gc.strokePolygon(xs, ys, n);
        } else {
            gc.strokePolyline(xs, ys, n);
        }
    }
}
