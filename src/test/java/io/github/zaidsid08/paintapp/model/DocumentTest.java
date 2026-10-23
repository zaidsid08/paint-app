// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class DocumentTest {

    private static RectShape rect(long id, double x) {
        return new RectShape(id, new Bounds(x, 0, x + 10, 10), new Style(Rgba.BLACK, Rgba.WHITE, 1));
    }

    @Test
    void topmostAtPrefersTheFrontElement() {
        Document d = new Document();
        d.add(rect(1, 0));
        d.add(rect(2, 5)); // overlaps rect 1 between x=5 and x=10
        assertEquals(2, d.topmostAt(new Vec2(7, 5), 0).orElseThrow().id());
        assertEquals(1, d.topmostAt(new Vec2(2, 5), 0).orElseThrow().id());
        assertTrue(d.topmostAt(new Vec2(50, 50), 0).isEmpty());
    }

    @Test
    void newIdsNeverCollideWithLoadedElements() {
        Document d = new Document();
        d.add(rect(41, 0));
        assertEquals(42, d.newId());
        assertEquals(43, d.newId());
    }

    @Test
    void duplicateIdsAndMissingIdsAreErrors() {
        Document d = new Document();
        d.add(rect(1, 0));
        assertThrows(IllegalArgumentException.class, () -> d.add(rect(1, 20)));
        assertThrows(IllegalArgumentException.class, () -> d.remove(99));
        assertThrows(IllegalArgumentException.class, () -> d.replace(rect(99, 0)));
    }

    @Test
    void moveChangesZOrder() {
        Document d = new Document();
        d.add(rect(1, 0));
        d.add(rect(2, 0));
        d.add(rect(3, 0));
        d.move(3, 0);
        assertEquals(0, d.indexOf(3));
        assertEquals(2, d.indexOf(2));
    }

    @Test
    void withinOnlyReturnsFullyContainedElements() {
        Document d = new Document();
        d.add(rect(1, 0));
        d.add(rect(2, 30));
        assertEquals(1, d.within(new Bounds(-1, -1, 25, 11)).size());
    }

    @Test
    void listenersHearEveryChange() {
        Document d = new Document();
        AtomicInteger calls = new AtomicInteger();
        d.addListener(doc -> calls.incrementAndGet());
        d.add(rect(1, 0));
        d.replace(rect(1, 5));
        d.move(1, 0);
        d.remove(1);
        assertEquals(4, calls.get());
    }
}
