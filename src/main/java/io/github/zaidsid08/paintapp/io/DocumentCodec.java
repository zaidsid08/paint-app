// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.io;

import io.github.zaidsid08.paintapp.model.Bounds;
import io.github.zaidsid08.paintapp.model.Element;
import io.github.zaidsid08.paintapp.model.EllipseShape;
import io.github.zaidsid08.paintapp.model.FreehandStroke;
import io.github.zaidsid08.paintapp.model.LineSegment;
import io.github.zaidsid08.paintapp.model.PolyShape;
import io.github.zaidsid08.paintapp.model.RectShape;
import io.github.zaidsid08.paintapp.model.Rgba;
import io.github.zaidsid08.paintapp.model.Style;
import io.github.zaidsid08.paintapp.model.Vec2;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reads and writes the {@code .paint} drawing format: a plain-text, line-based
 * format that diffs well in git and is easy to inspect by hand.
 *
 * <pre>
 * paintapp 1
 * # kind id key=value ...
 * rect 1 stroke=#000000ff fill=none width=3 pts=10,10;110,60
 * pen 2 stroke=#1e66f5ff fill=none width=2 pts=0,0;5,3;9,8
 * poly 3 stroke=#000000ff fill=#ffcc00ff width=1 closed=true pts=0,0;40,0;20,30
 * </pre>
 *
 * <p>Elements are listed back to front. Unknown keys are rejected so typos
 * don't silently lose data.
 */
public final class DocumentCodec {

    static final String HEADER = "paintapp 1";
    private static final Set<String> COMMON_KEYS = Set.of("stroke", "fill", "width", "pts");

    private DocumentCodec() {
    }

    // ---- writing ---------------------------------------------------------

    public static String write(List<Element> elements) {
        StringBuilder sb = new StringBuilder(HEADER).append('\n');
        for (Element e : elements) {
            sb.append(encode(e)).append('\n');
        }
        return sb.toString();
    }

    static String encode(Element e) {
        return switch (e) {
            case FreehandStroke s -> line("pen", s, s.points(), "");
            case LineSegment l -> line("line", l, List.of(l.start(), l.end()), "");
            case RectShape r -> line("rect", r, corners(r.box()), "");
            case EllipseShape el -> line("ellipse", el, corners(el.box()), "");
            case PolyShape p -> line("poly", p, p.vertices(), " closed=" + p.closed());
        };
    }

    private static List<Vec2> corners(Bounds b) {
        return List.of(new Vec2(b.minX(), b.minY()), new Vec2(b.maxX(), b.maxY()));
    }

    private static String line(String kind, Element e, List<Vec2> pts, String extra) {
        Style s = e.style();
        String points = pts.stream().map(p -> num(p.x()) + "," + num(p.y()))
                .collect(Collectors.joining(";"));
        return kind + " " + e.id()
                + " stroke=" + s.stroke().toHex()
                + " fill=" + (s.fill() == null ? "none" : s.fill().toHex())
                + " width=" + num(s.strokeWidth())
                + extra
                + " pts=" + points;
    }

    /** Up to two decimals, no trailing zeros, always '.' as the separator. */
    static String num(double v) {
        BigDecimal d = BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        return d.scale() < 0 ? d.setScale(0).toPlainString() : d.toPlainString();
    }

    // ---- reading ---------------------------------------------------------

    public static List<Element> read(String text) throws FormatException {
        try {
            return read(new StringReader(text));
        } catch (FormatException e) {
            throw e;
        } catch (IOException e) {
            throw new IllegalStateException(e); // StringReader doesn't fail
        }
    }

    public static List<Element> read(Reader source) throws IOException {
        BufferedReader in = new BufferedReader(source);
        String first = in.readLine();
        if (first == null || !first.strip().equals(HEADER)) {
            throw new FormatException(1, "not a paintapp file (expected '" + HEADER + "')");
        }
        List<Element> out = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        int lineNo = 1;
        String raw;
        while ((raw = in.readLine()) != null) {
            lineNo++;
            String text = raw.strip();
            if (text.isEmpty() || text.startsWith("#")) {
                continue;
            }
            Element e = decode(text, lineNo);
            if (!seen.add(e.id())) {
                throw new FormatException(lineNo, "duplicate id " + e.id());
            }
            out.add(e);
        }
        return out;
    }

    private static Element decode(String text, int lineNo) throws FormatException {
        String[] parts = text.split("\\s+");
        if (parts.length < 2) {
            throw new FormatException(lineNo, "expected '<kind> <id> ...'");
        }
        String kind = parts[0];
        long id;
        try {
            id = Long.parseLong(parts[1]);
        } catch (NumberFormatException ex) {
            throw new FormatException(lineNo, "bad id '" + parts[1] + "'");
        }
        if (id <= 0) {
            throw new FormatException(lineNo, "id must be positive");
        }

        Map<String, String> kv = new HashMap<>();
        for (int i = 2; i < parts.length; i++) {
            int eq = parts[i].indexOf('=');
            if (eq <= 0) {
                throw new FormatException(lineNo, "expected key=value, got '" + parts[i] + "'");
            }
            String key = parts[i].substring(0, eq);
            boolean allowed = COMMON_KEYS.contains(key) || (kind.equals("poly") && key.equals("closed"));
            if (!allowed) {
                throw new FormatException(lineNo, "unknown key '" + key + "' for " + kind);
            }
            kv.put(key, parts[i].substring(eq + 1));
        }

        try {
            Style style = new Style(
                    Rgba.fromHex(require(kv, "stroke", lineNo)),
                    "none".equals(kv.getOrDefault("fill", "none")) ? null : Rgba.fromHex(kv.get("fill")),
                    Double.parseDouble(kv.getOrDefault("width", "1")));
            List<Vec2> pts = parsePoints(require(kv, "pts", lineNo), lineNo);
            return switch (kind) {
                case "pen" -> new FreehandStroke(id, pts, style);
                case "line" -> new LineSegment(id, exactly(pts, 2, lineNo).get(0), pts.get(1), style);
                case "rect" -> new RectShape(id, Bounds.spanning(exactly(pts, 2, lineNo).get(0), pts.get(1)), style);
                case "ellipse" -> new EllipseShape(id, Bounds.spanning(exactly(pts, 2, lineNo).get(0), pts.get(1)), style);
                case "poly" -> new PolyShape(id, pts, Boolean.parseBoolean(kv.getOrDefault("closed", "false")), style);
                default -> throw new FormatException(lineNo, "unknown element kind '" + kind + "'");
            };
        } catch (IllegalArgumentException ex) {
            // Bad numbers, bad colours, and constructor validation all land here.
            throw new FormatException(lineNo, ex.getMessage());
        }
    }

    private static String require(Map<String, String> kv, String key, int lineNo) throws FormatException {
        String v = kv.get(key);
        if (v == null) {
            throw new FormatException(lineNo, "missing '" + key + "'");
        }
        return v;
    }

    private static List<Vec2> exactly(List<Vec2> pts, int n, int lineNo) throws FormatException {
        if (pts.size() != n) {
            throw new FormatException(lineNo, "expected " + n + " points, got " + pts.size());
        }
        return pts;
    }

    private static List<Vec2> parsePoints(String s, int lineNo) throws FormatException {
        List<Vec2> out = new ArrayList<>();
        for (String pair : s.split(";")) {
            String[] xy = pair.split(",");
            if (xy.length != 2) {
                throw new FormatException(lineNo, "bad point '" + pair + "'");
            }
            double x = Double.parseDouble(xy[0]);
            double y = Double.parseDouble(xy[1]);
            if (!Double.isFinite(x) || !Double.isFinite(y)) {
                throw new FormatException(lineNo, "non-finite coordinate in '" + pair + "'");
            }
            out.add(new Vec2(x, y));
        }
        return out;
    }
}
