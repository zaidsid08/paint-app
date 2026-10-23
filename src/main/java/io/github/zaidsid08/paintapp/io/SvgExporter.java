// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.io;

import static io.github.zaidsid08.paintapp.io.DocumentCodec.num;

import io.github.zaidsid08.paintapp.model.Bounds;
import io.github.zaidsid08.paintapp.model.Element;
import io.github.zaidsid08.paintapp.model.EllipseShape;
import io.github.zaidsid08.paintapp.model.FreehandStroke;
import io.github.zaidsid08.paintapp.model.LineSegment;
import io.github.zaidsid08.paintapp.model.PolyShape;
import io.github.zaidsid08.paintapp.model.RectShape;
import io.github.zaidsid08.paintapp.model.Rgba;
import io.github.zaidsid08.paintapp.model.Smoothing;
import io.github.zaidsid08.paintapp.model.Style;
import io.github.zaidsid08.paintapp.model.Vec2;
import java.util.List;
import java.util.stream.Collectors;

/** Exports a drawing as a standalone SVG, cropped to the content plus a margin. */
public final class SvgExporter {

    static final double MARGIN = 10;

    private SvgExporter() {
    }

    public static String export(List<Element> elements) {
        Bounds box = elements.stream().map(Element::paintedBounds).reduce(Bounds::union)
                .orElse(new Bounds(0, 0, 100, 100)).inflate(MARGIN);

        StringBuilder sb = new StringBuilder();
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\"")
                .append(" width=\"").append(num(box.width())).append('"')
                .append(" height=\"").append(num(box.height())).append('"')
                .append(" viewBox=\"").append(num(box.minX())).append(' ').append(num(box.minY()))
                .append(' ').append(num(box.width())).append(' ').append(num(box.height())).append("\">\n");
        for (Element e : elements) {
            sb.append("  ").append(toSvg(e)).append('\n');
        }
        return sb.append("</svg>\n").toString();
    }

    static String toSvg(Element e) {
        return switch (e) {
            case FreehandStroke s -> s.points().size() == 1
                    ? dot(s.points().get(0), s.style())
                    : "<path d=\"" + pathData(s.points()) + "\"" + paint(s.style(), false) + "/>";
            case LineSegment l -> "<line x1=\"" + num(l.start().x()) + "\" y1=\"" + num(l.start().y())
                    + "\" x2=\"" + num(l.end().x()) + "\" y2=\"" + num(l.end().y()) + "\""
                    + paint(l.style(), false) + "/>";
            case RectShape r -> "<rect x=\"" + num(r.box().minX()) + "\" y=\"" + num(r.box().minY())
                    + "\" width=\"" + num(r.box().width()) + "\" height=\"" + num(r.box().height()) + "\""
                    + paint(r.style(), true) + "/>";
            case EllipseShape el -> "<ellipse cx=\"" + num(el.box().center().x()) + "\" cy=\""
                    + num(el.box().center().y()) + "\" rx=\"" + num(el.box().width() / 2) + "\" ry=\""
                    + num(el.box().height() / 2) + "\"" + paint(el.style(), true) + "/>";
            case PolyShape p -> "<" + (p.closed() ? "polygon" : "polyline") + " points=\""
                    + p.vertices().stream().map(v -> num(v.x()) + "," + num(v.y())).collect(Collectors.joining(" "))
                    + "\"" + paint(p.style(), p.closed()) + "/>";
        };
    }

    private static String dot(Vec2 p, Style s) {
        return "<circle cx=\"" + num(p.x()) + "\" cy=\"" + num(p.y()) + "\" r=\"" + num(s.strokeWidth() / 2)
                + "\" fill=\"" + s.stroke().toHexRgb() + "\"" + opacity("fill-opacity", s.stroke()) + "/>";
    }

    private static String pathData(List<Vec2> pts) {
        StringBuilder d = new StringBuilder("M").append(num(pts.get(0).x())).append(' ').append(num(pts.get(0).y()));
        for (Smoothing.Cubic c : Smoothing.catmullRom(pts)) {
            d.append(" C").append(num(c.c1().x())).append(' ').append(num(c.c1().y()))
                    .append(' ').append(num(c.c2().x())).append(' ').append(num(c.c2().y()))
                    .append(' ').append(num(c.to().x())).append(' ').append(num(c.to().y()));
        }
        return d.toString();
    }

    private static String paint(Style s, boolean fillable) {
        StringBuilder a = new StringBuilder();
        Rgba fill = fillable ? s.fill() : null;
        a.append(" fill=\"").append(fill == null ? "none" : fill.toHexRgb()).append('"');
        if (fill != null) {
            a.append(opacity("fill-opacity", fill));
        }
        a.append(" stroke=\"").append(s.stroke().toHexRgb()).append('"')
                .append(opacity("stroke-opacity", s.stroke()))
                .append(" stroke-width=\"").append(num(s.strokeWidth())).append('"')
                .append(" stroke-linecap=\"round\" stroke-linejoin=\"round\"");
        return a.toString();
    }

    private static String opacity(String attr, Rgba c) {
        return c.a() < 1 ? " " + attr + "=\"" + num(c.a()) + "\"" : "";
    }
}
