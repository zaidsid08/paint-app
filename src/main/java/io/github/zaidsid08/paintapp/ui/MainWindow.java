// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.ui;

import io.github.zaidsid08.paintapp.command.Restack;
import io.github.zaidsid08.paintapp.editor.Editor;
import io.github.zaidsid08.paintapp.model.Bounds;
import io.github.zaidsid08.paintapp.model.Element;
import io.github.zaidsid08.paintapp.model.Vec2;
import io.github.zaidsid08.paintapp.tool.Key;
import io.github.zaidsid08.paintapp.tool.Tool;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/** Builds the window (menus, tool bar, canvas, style panel, status bar) and wires actions to the Editor. */
final class MainWindow {

    /** Single-key shortcuts for tools, shown in their tooltips. */
    private static final Map<String, KeyCode> TOOL_KEYS = Map.of(
            "Select", KeyCode.V, "Pen", KeyCode.P, "Line", KeyCode.L, "Rectangle", KeyCode.R,
            "Ellipse", KeyCode.E, "Polyline", KeyCode.O, "Eraser", KeyCode.X);

    private static final FileChooser.ExtensionFilter DRAWING_FILES =
            new FileChooser.ExtensionFilter("Paint App drawing (*.paint)", "*.paint");

    private final Stage stage;
    private final Editor editor = new Editor();
    private final CanvasView canvas = new CanvasView(editor);
    private final ToggleGroup toolGroup = new ToggleGroup();
    private final Label hintLabel = new Label();
    private final Label positionLabel = new Label();
    private final Label zoomLabel = new Label();
    private final MenuItem undoItem = new MenuItem("Undo");
    private final MenuItem redoItem = new MenuItem("Redo");

    MainWindow(Stage stage) {
        this.stage = stage;
        editor.setRepaintCallback(canvas::repaint);

        BorderPane root = new BorderPane();
        root.setTop(new VBox(buildMenuBar(), buildToolBar()));
        root.setCenter(canvas);
        root.setRight(new StylePanel(editor));
        root.setBottom(buildStatusBar());

        Scene scene = new Scene(root, 1100, 720);
        scene.getStylesheets().add(MainWindow.class.getResource("paintapp.css").toExternalForm());
        scene.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
        scene.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.SPACE) {
                canvas.setSpaceHeld(false);
            }
        });

        canvas.setPointerListener(this::showPosition);
        editor.history().addListener(h -> refreshChrome());
        editor.selection().addListener(s -> refreshChrome());

        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            if (!confirmDiscard()) {
                e.consume();
            }
        });
        selectTool("Pen");
        refreshChrome();
    }

    void show() {
        stage.show();
        canvas.repaint();
    }

    // ---- layout ---------------------------------------------------------------

    private MenuBar buildMenuBar() {
        Menu file = new Menu("File");
        file.getItems().addAll(
                item("New", "Shortcut+N", this::newDrawing),
                item("Open…", "Shortcut+O", this::openDrawing),
                item("Save", "Shortcut+S", () -> save(false)),
                item("Save As…", "Shortcut+Shift+S", () -> save(true)),
                new SeparatorMenuItem(),
                item("Export SVG…", "Shortcut+Shift+E", this::exportSvg),
                item("Export PNG…", null, this::exportPng),
                new SeparatorMenuItem(),
                item("Quit", "Shortcut+Q", () -> {
                    if (confirmDiscard()) {
                        stage.close();
                    }
                }));

        undoItem.setAccelerator(KeyCombination.keyCombination("Shortcut+Z"));
        undoItem.setOnAction(e -> editor.undo());
        redoItem.setAccelerator(KeyCombination.keyCombination("Shortcut+Shift+Z"));
        redoItem.setOnAction(e -> editor.redo());

        Menu edit = new Menu("Edit");
        edit.getItems().addAll(undoItem, redoItem, new SeparatorMenuItem(),
                item("Duplicate", "Shortcut+D", editor::duplicateSelection),
                item("Delete", null, editor::deleteSelection),
                item("Select All", "Shortcut+A", editor::selectAll),
                new SeparatorMenuItem(),
                item("Bring Forward", "Shortcut+CLOSE_BRACKET", () -> editor.restack(Restack.Direction.FORWARD)),
                item("Send Backward", "Shortcut+OPEN_BRACKET", () -> editor.restack(Restack.Direction.BACKWARD)),
                item("Bring to Front", "Shortcut+Shift+CLOSE_BRACKET", () -> editor.restack(Restack.Direction.TO_FRONT)),
                item("Send to Back", "Shortcut+Shift+OPEN_BRACKET", () -> editor.restack(Restack.Direction.TO_BACK)));

        Menu view = new Menu("View");
        view.getItems().addAll(
                item("Zoom In", "Shortcut+EQUALS", () -> zoom(1.25)),
                item("Zoom Out", "Shortcut+MINUS", () -> zoom(0.8)),
                item("Actual Size", "Shortcut+DIGIT0", () -> {
                    editor.viewport().reset();
                    canvas.repaint();
                    refreshChrome();
                }));

        return new MenuBar(file, edit, view);
    }

    private static MenuItem item(String text, String accelerator, Runnable action) {
        MenuItem mi = new MenuItem(text);
        if (accelerator != null) {
            mi.setAccelerator(KeyCombination.keyCombination(accelerator));
        }
        mi.setOnAction(e -> action.run());
        return mi;
    }

    private ToolBar buildToolBar() {
        ToolBar bar = new ToolBar();
        for (Tool t : editor.tools()) {
            ToggleButton b = new ToggleButton(t.name());
            b.setUserData(t.name());
            b.setToggleGroup(toolGroup);
            b.setTooltip(new Tooltip(t.name() + " (" + TOOL_KEYS.get(t.name()).getName() + ")"));
            b.setOnAction(e -> selectTool(t.name()));
            bar.getItems().add(b);
        }
        // Keep exactly one tool selected even if the active button is clicked again.
        toolGroup.selectedToggleProperty().addListener((o, was, now) -> {
            if (now == null && was != null) {
                was.setSelected(true);
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button undo = new Button("Undo");
        undo.setOnAction(e -> editor.undo());
        undo.disableProperty().bind(undoItem.disableProperty());
        Button redo = new Button("Redo");
        redo.setOnAction(e -> editor.redo());
        redo.disableProperty().bind(redoItem.disableProperty());
        bar.getItems().addAll(spacer, undo, redo);
        return bar;
    }

    private HBox buildStatusBar() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox status = new HBox(16, hintLabel, spacer, positionLabel, zoomLabel);
        status.setAlignment(Pos.CENTER_LEFT);
        status.setPadding(new Insets(4, 10, 4, 10));
        status.getStyleClass().add("status-bar");
        return status;
    }

    // ---- input ----------------------------------------------------------------

    private void onKeyPressed(KeyEvent e) {
        if (e.isShortcutDown() || e.isAltDown()) {
            return; // menu accelerators handle these
        }
        KeyCode code = e.getCode();
        Key toolKey = switch (code) {
            case ENTER -> Key.ENTER;
            case ESCAPE -> Key.ESCAPE;
            case LEFT -> Key.LEFT;
            case RIGHT -> Key.RIGHT;
            case UP -> Key.UP;
            case DOWN -> Key.DOWN;
            default -> null;
        };
        if (toolKey != null) {
            editor.tool().key(toolKey, e.isShiftDown());
            e.consume();
            return;
        }
        if (code == KeyCode.DELETE || code == KeyCode.BACK_SPACE) {
            editor.deleteSelection();
            e.consume();
        } else if (code == KeyCode.SPACE) {
            canvas.setSpaceHeld(true);
            e.consume();
        } else {
            TOOL_KEYS.forEach((name, key) -> {
                if (key == code) {
                    selectTool(name);
                    e.consume();
                }
            });
        }
    }

    private void selectTool(String name) {
        editor.selectTool(name);
        toolGroup.getToggles().stream()
                .filter(t -> name.equals(t.getUserData()))
                .findFirst()
                .ifPresent(t -> t.setSelected(true));
        refreshChrome();
    }

    private void zoom(double factor) {
        editor.viewport().zoomAt(new Vec2(canvas.getWidth() / 2, canvas.getHeight() / 2), factor);
        canvas.repaint();
        refreshChrome();
    }

    private void showPosition(Vec2 p) {
        positionLabel.setText(String.format("%.0f, %.0f", p.x(), p.y()));
        zoomLabel.setText(Math.round(editor.viewport().scale() * 100) + "%");
    }

    /** Title, undo/redo labels and status text. */
    private void refreshChrome() {
        String name = editor.file().map(p -> p.getFileName().toString()).orElse("Untitled");
        stage.setTitle((editor.isDirty() ? "• " : "") + name + " — Paint App");

        undoItem.setText(editor.history().undoLabel().map(l -> "Undo " + l).orElse("Undo"));
        undoItem.setDisable(!editor.history().canUndo());
        redoItem.setText(editor.history().redoLabel().map(l -> "Redo " + l).orElse("Redo"));
        redoItem.setDisable(!editor.history().canRedo());

        int selected = editor.selection().size();
        hintLabel.setText(editor.tool().hint()
                + (selected > 0 ? "   ·   " + selected + " selected" : ""));
        zoomLabel.setText(Math.round(editor.viewport().scale() * 100) + "%");
    }

    // ---- files ----------------------------------------------------------------

    private void newDrawing() {
        if (confirmDiscard()) {
            editor.newDocument();
            refreshChrome();
        }
    }

    private void openDrawing() {
        if (!confirmDiscard()) {
            return;
        }
        FileChooser fc = chooser("Open drawing", DRAWING_FILES);
        File f = fc.showOpenDialog(stage);
        if (f == null) {
            return;
        }
        try {
            editor.open(f.toPath());
        } catch (IOException ex) {
            error("Couldn't open " + f.getName(), ex.getMessage());
        }
        refreshChrome();
    }

    /** @return true if the drawing was saved */
    private boolean save(boolean askForPath) {
        Optional<Path> target = editor.file();
        if (askForPath || target.isEmpty()) {
            File f = chooser("Save drawing", DRAWING_FILES).showSaveDialog(stage);
            if (f == null) {
                return false;
            }
            target = Optional.of(withExtension(f.toPath(), ".paint"));
        }
        try {
            editor.save(target.get());
            refreshChrome();
            return true;
        } catch (IOException ex) {
            error("Couldn't save", ex.getMessage());
            return false;
        }
    }

    private void exportSvg() {
        File f = chooser("Export SVG", new FileChooser.ExtensionFilter("SVG image (*.svg)", "*.svg"))
                .showSaveDialog(stage);
        if (f == null) {
            return;
        }
        try {
            editor.exportSvg(withExtension(f.toPath(), ".svg"));
        } catch (IOException ex) {
            error("Couldn't export SVG", ex.getMessage());
        }
    }

    private void exportPng() {
        List<Element> elements = editor.document().elements();
        if (elements.isEmpty()) {
            error("Nothing to export", "The drawing is empty.");
            return;
        }
        File f = chooser("Export PNG", new FileChooser.ExtensionFilter("PNG image (*.png)", "*.png"))
                .showSaveDialog(stage);
        if (f == null) {
            return;
        }
        // Render the content (not the viewport) at 2x onto an offscreen canvas.
        double scale = 2, margin = 10;
        Bounds box = elements.stream().map(Element::paintedBounds).reduce(Bounds::union).orElseThrow().inflate(margin);
        Canvas off = new Canvas(Math.ceil(box.width() * scale), Math.ceil(box.height() * scale));
        GraphicsContext gc = off.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, off.getWidth(), off.getHeight());
        gc.setTransform(new Affine(scale, 0, -box.minX() * scale, 0, scale, -box.minY() * scale));
        Renderer.paintElements(gc, elements);

        WritableImage image = off.snapshot(new SnapshotParameters(), null);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", withExtension(f.toPath(), ".png").toFile());
        } catch (IOException ex) {
            error("Couldn't export PNG", ex.getMessage());
        }
    }

    /** Ask about unsaved changes. Returns false if the user cancelled. */
    private boolean confirmDiscard() {
        if (!editor.isDirty()) {
            return true;
        }
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.YES);
        ButtonType discardBtn = new ButtonType("Don't Save", ButtonBar.ButtonData.NO);
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Save changes to your drawing first?",
                saveBtn, discardBtn, ButtonType.CANCEL);
        a.setHeaderText("You have unsaved changes");
        a.initOwner(stage);
        ButtonType choice = a.showAndWait().orElse(ButtonType.CANCEL);
        if (choice == saveBtn) {
            return save(false);
        }
        return choice == discardBtn;
    }

    private FileChooser chooser(String title, FileChooser.ExtensionFilter filter) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(filter);
        editor.file().map(Path::getParent).map(Path::toFile).ifPresent(fc::setInitialDirectory);
        return fc;
    }

    private static Path withExtension(Path p, String ext) {
        String name = p.getFileName().toString();
        return name.toLowerCase().endsWith(ext) ? p : p.resolveSibling(name + ext);
    }

    private void error(String header, String detail) {
        Alert a = new Alert(Alert.AlertType.ERROR, detail);
        a.setHeaderText(header);
        a.initOwner(stage);
        a.showAndWait();
    }
}
