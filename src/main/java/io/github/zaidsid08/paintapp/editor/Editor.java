// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.editor;

import io.github.zaidsid08.paintapp.command.AddElements;
import io.github.zaidsid08.paintapp.command.CommandHistory;
import io.github.zaidsid08.paintapp.command.RemoveElements;
import io.github.zaidsid08.paintapp.command.ReplaceElements;
import io.github.zaidsid08.paintapp.command.Restack;
import io.github.zaidsid08.paintapp.io.DocumentCodec;
import io.github.zaidsid08.paintapp.io.SvgExporter;
import io.github.zaidsid08.paintapp.model.Bounds;
import io.github.zaidsid08.paintapp.model.Document;
import io.github.zaidsid08.paintapp.model.Element;
import io.github.zaidsid08.paintapp.model.Selection;
import io.github.zaidsid08.paintapp.model.Style;
import io.github.zaidsid08.paintapp.model.Vec2;
import io.github.zaidsid08.paintapp.tool.EllipseTool;
import io.github.zaidsid08.paintapp.tool.EraserTool;
import io.github.zaidsid08.paintapp.tool.LineTool;
import io.github.zaidsid08.paintapp.tool.PenTool;
import io.github.zaidsid08.paintapp.tool.PolylineTool;
import io.github.zaidsid08.paintapp.tool.RectTool;
import io.github.zaidsid08.paintapp.tool.SelectTool;
import io.github.zaidsid08.paintapp.tool.Tool;
import io.github.zaidsid08.paintapp.tool.ToolContext;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The application's controller, minus the UI toolkit. It owns the document,
 * history, selection, current tool and style, and exposes every user action
 * as a plain method. The JavaFX layer only translates input into these calls
 * and paints what's here, so all behaviour can be unit-tested headlessly.
 */
public final class Editor implements ToolContext {

    /** Screen pixels a click may miss by and still hit an element. */
    static final double HIT_SLOP_PX = 5;
    static final int HISTORY_LIMIT = 500;
    static final Vec2 DUPLICATE_OFFSET = new Vec2(12, 12);

    private final Document document = new Document();
    private final CommandHistory history = new CommandHistory(document, HISTORY_LIMIT);
    private final Selection selection = new Selection();
    private final Viewport viewport = new Viewport();
    private final Map<String, Tool> tools = new LinkedHashMap<>();

    private Tool tool;
    private Style style = Style.DEFAULT;
    private Element preview;
    private Bounds marquee;
    private Set<Long> highlighted = Set.of();
    private Path file;
    private Runnable repaint = () -> { };

    public Editor() {
        register(new SelectTool(this));
        register(new PenTool(this));
        register(new LineTool(this));
        register(new RectTool(this));
        register(new EllipseTool(this));
        register(new PolylineTool(this));
        register(new EraserTool(this));
        tool = tools.get("Pen");

        document.addListener(d -> repaint.run());
        selection.addListener(s -> repaint.run());
        history.addListener(h -> selection.retainExisting(document));
    }

    private void register(Tool t) {
        tools.put(t.name(), t);
    }

    public void setRepaintCallback(Runnable r) {
        this.repaint = r;
    }

    // ---- ToolContext -------------------------------------------------------

    @Override
    public Document document() {
        return document;
    }

    @Override
    public CommandHistory history() {
        return history;
    }

    @Override
    public Selection selection() {
        return selection;
    }

    @Override
    public Style currentStyle() {
        return style;
    }

    @Override
    public double hitTolerance() {
        return HIT_SLOP_PX / viewport.scale();
    }

    @Override
    public void setPreview(Element preview) {
        this.preview = preview;
        repaint.run();
    }

    @Override
    public void setMarquee(Bounds marquee) {
        this.marquee = marquee;
        repaint.run();
    }

    @Override
    public void setHighlighted(Set<Long> ids) {
        this.highlighted = Set.copyOf(ids);
        repaint.run();
    }

    @Override
    public void requestRepaint() {
        repaint.run();
    }

    // ---- state the view paints --------------------------------------------

    public Optional<Element> preview() {
        return Optional.ofNullable(preview);
    }

    public Optional<Bounds> marquee() {
        return Optional.ofNullable(marquee);
    }

    public Set<Long> highlighted() {
        return highlighted;
    }

    public Viewport viewport() {
        return viewport;
    }

    // ---- tools and style ----------------------------------------------------

    public List<Tool> tools() {
        return List.copyOf(tools.values());
    }

    public Tool tool() {
        return tool;
    }

    public void selectTool(String name) {
        Tool next = tools.get(name);
        if (next == null) {
            throw new IllegalArgumentException("no tool named " + name);
        }
        if (next != tool) {
            tool.deactivate();
            tool = next;
            repaint.run();
        }
    }

    /**
     * Sets the style for new elements and, if anything is selected, applies
     * it to the selection as one undoable step.
     */
    public void setStyle(Style newStyle) {
        this.style = newStyle;
        List<Element> before = selection.elementsIn(document).stream()
                .filter(e -> !e.style().equals(newStyle))
                .toList();
        if (!before.isEmpty()) {
            List<Element> after = before.stream().map(e -> e.withStyle(newStyle)).toList();
            history.execute(new ReplaceElements(before, after, "Change style"));
        }
    }

    // ---- edit actions -------------------------------------------------------

    public void undo() {
        tool.deactivate();
        history.undo();
    }

    public void redo() {
        tool.deactivate();
        history.redo();
    }

    public void deleteSelection() {
        if (!selection.isEmpty()) {
            history.execute(new RemoveElements(selection.ids()));
            selection.clear();
        }
    }

    public void selectAll() {
        selection.set(document.elements().stream().map(Element::id).toList());
    }

    /** Copies the selection, offset slightly, and selects the copies. */
    public void duplicateSelection() {
        List<Element> source = selection.elementsIn(document);
        if (source.isEmpty()) {
            return;
        }
        List<Element> copies = new ArrayList<>();
        for (Element e : source) {
            copies.add(e.translate(DUPLICATE_OFFSET).withId(document.newId()));
        }
        history.execute(new AddElements(copies, "Duplicate"));
        selection.set(copies.stream().map(Element::id).toList());
    }

    public void restack(Restack.Direction direction) {
        if (!selection.isEmpty()) {
            history.execute(new Restack(selection.ids(), direction));
        }
    }

    // ---- files ---------------------------------------------------------------

    public Optional<Path> file() {
        return Optional.ofNullable(file);
    }

    public boolean isDirty() {
        return history.isDirty();
    }

    public void newDocument() {
        tool.deactivate();
        selection.clear();
        document.clear();
        history.reset();
        viewport.reset();
        file = null;
    }

    public void open(Path path) throws IOException {
        List<Element> loaded;
        try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            loaded = DocumentCodec.read(r);
        }
        // Parse fully before touching the current document, so a bad file
        // leaves the user's work intact.
        newDocument();
        loaded.forEach(document::add);
        history.reset();
        file = path;
    }

    public void save(Path path) throws IOException {
        Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(tmp, DocumentCodec.write(document.elements()), StandardCharsets.UTF_8);
        Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
        file = path;
        history.markClean();
    }

    public void exportSvg(Path path) throws IOException {
        Files.writeString(path, SvgExporter.export(document.elements()), StandardCharsets.UTF_8);
    }
}
