// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.zaidsid08.paintapp.model.Bounds;
import io.github.zaidsid08.paintapp.model.FreehandStroke;
import io.github.zaidsid08.paintapp.model.RectShape;
import io.github.zaidsid08.paintapp.model.Rgba;
import io.github.zaidsid08.paintapp.model.Style;
import io.github.zaidsid08.paintapp.model.Vec2;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

class SvgExporterTest {

    @Test
    void exportIsWellFormedXmlWithOneNodePerElement() throws Exception {
        String svg = SvgExporter.export(DocumentCodecTest.sample());
        Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)));
        assertEquals("svg", xml.getDocumentElement().getTagName());
        assertEquals(6, xml.getDocumentElement().getElementsByTagName("*").getLength());
        assertEquals(1, xml.getElementsByTagName("polygon").getLength());
        assertEquals(1, xml.getElementsByTagName("polyline").getLength());
    }

    @Test
    void viewBoxCropsToContentPlusMargin() {
        RectShape r = new RectShape(1, new Bounds(100, 200, 150, 220), new Style(Rgba.BLACK, null, 2));
        String svg = SvgExporter.export(List.of(r));
        // painted bounds are 99..151 x 199..221, plus a 10 unit margin
        assertTrue(svg.contains("viewBox=\"89 189 72 42\""), svg);
    }

    @Test
    void translucentColoursBecomeOpacityAttributes() {
        Style s = new Style(Rgba.fromHex("#ff000080"), Rgba.fromHex("#00ff00"), 1);
        String rect = SvgExporter.toSvg(new RectShape(1, new Bounds(0, 0, 1, 1), s));
        assertTrue(rect.contains("stroke=\"#ff0000\" stroke-opacity=\"0.5\""), rect);
        assertTrue(rect.contains("fill=\"#00ff00\""), rect);
        assertTrue(!rect.contains("fill-opacity"), rect);
    }

    @Test
    void freehandUsesSmoothCurvesAndSinglePointsBecomeDots() {
        Style s = Style.DEFAULT;
        String path = SvgExporter.toSvg(new FreehandStroke(1, List.of(Vec2.ZERO, new Vec2(10, 0), new Vec2(20, 5)), s));
        assertTrue(path.startsWith("<path d=\"M0 0 C"), path);
        String dot = SvgExporter.toSvg(new FreehandStroke(2, List.of(new Vec2(4, 4)), s));
        assertTrue(dot.startsWith("<circle cx=\"4\" cy=\"4\" r=\"1.5\""), dot);
    }
}
