// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DocumentCodecTest {

    private static final Style A = new Style(Rgba.fromHex("#1e66f5ff"), null, 2.5);
    private static final Style B = new Style(Rgba.BLACK, Rgba.fromHex("#ffd16680"), 1);

    static List<Element> sample() {
        return List.of(
                new FreehandStroke(1, List.of(new Vec2(0, 0), new Vec2(3.25, 4), new Vec2(9, -2)), A),
                new LineSegment(2, new Vec2(-5, 5), new Vec2(40, 0.5), A),
                new RectShape(3, new Bounds(10, 10, 110, 60), B),
                new EllipseShape(4, new Bounds(0, 0, 30, 20), B),
                new PolyShape(5, List.of(new Vec2(0, 0), new Vec2(40, 0), new Vec2(20, 30)), true, B),
                new PolyShape(6, List.of(new Vec2(1, 1), new Vec2(2, 2)), false, A));
    }

    @Test
    void roundTripPreservesEveryElementAndOrder() throws Exception {
        List<Element> original = sample();
        assertEquals(original, DocumentCodec.read(DocumentCodec.write(original)));
    }

    @Test
    void outputIsReadableText() {
        String text = DocumentCodec.write(List.of(sample().get(2)));
        assertEquals("paintapp 1\nrect 3 stroke=#000000ff fill=#ffd16680 width=1 pts=10,10;110,60\n", text);
    }

    @Test
    void commentsAndBlankLinesAreIgnored() throws Exception {
        String text = "paintapp 1\n\n# a comment\nline 1 stroke=#000000 fill=none width=1 pts=0,0;1,1\n";
        assertEquals(1, DocumentCodec.read(text).size());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "line 1 stroke=#000000 pts=0,0                       | expected 2 points",
        "rect x stroke=#000000 pts=0,0;1,1                   | bad id",
        "rect 1 pts=0,0;1,1                                  | missing 'stroke'",
        "rect 1 stroke=#000000 colour=red pts=0,0;1,1        | unknown key",
        "blob 1 stroke=#000000 pts=0,0;1,1                   | unknown element kind",
        "rect 1 stroke=#nothex pts=0,0;1,1                   | bad colour",
        "rect 1 stroke=#000000 width=0 pts=0,0;1,1           | stroke width",
        "line 1 stroke=#000000 pts=0,0;NaN,1                 | non-finite",
    })
    void malformedLinesReportTheLineNumber(String line, String message) {
        FormatException e = assertThrows(FormatException.class,
                () -> DocumentCodec.read("paintapp 1\n# ok\n" + line + "\n"));
        assertEquals(3, e.line());
        assertTrue(e.getMessage().contains(message), e.getMessage());
    }

    @Test
    void wrongHeaderAndDuplicateIdsAreRejected() {
        assertThrows(FormatException.class, () -> DocumentCodec.read("hello\n"));
        assertThrows(FormatException.class, () -> DocumentCodec.read(""));
        String dup = "paintapp 1\nline 1 stroke=#000000 pts=0,0;1,1\nline 1 stroke=#000000 pts=0,0;1,1\n";
        assertEquals(3, assertThrows(FormatException.class, () -> DocumentCodec.read(dup)).line());
    }

    @Test
    void numbersAreCompactAndLocaleIndependent() {
        assertEquals("3", DocumentCodec.num(3.0));
        assertEquals("2.5", DocumentCodec.num(2.5));
        assertEquals("0.33", DocumentCodec.num(1.0 / 3));
        assertEquals("-1200", DocumentCodec.num(-1200));
    }
}
