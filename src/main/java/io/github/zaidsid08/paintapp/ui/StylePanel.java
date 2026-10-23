// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.ui;

import io.github.zaidsid08.paintapp.editor.Editor;
import io.github.zaidsid08.paintapp.model.Element;
import io.github.zaidsid08.paintapp.model.Style;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Stroke colour, fill and width controls. Edits apply to the selection (as
 * one undoable step) and become the style for new elements. Selecting a
 * single element loads its style into the controls.
 */
final class StylePanel extends VBox {

    private final Editor editor;
    private final ColorPicker strokePicker = new ColorPicker();
    private final CheckBox fillToggle = new CheckBox("Fill");
    private final ColorPicker fillPicker = new ColorPicker(Color.web("#ffd166"));
    private final Slider widthSlider = new Slider(0.5, 40, 3);
    private final Label widthLabel = new Label();
    private boolean syncing; // true while we push model state into the controls

    StylePanel(Editor editor) {
        this.editor = editor;
        setSpacing(8);
        setPadding(new Insets(12));
        setPrefWidth(190);
        getStyleClass().add("style-panel");

        Label heading = new Label("Style");
        heading.getStyleClass().add("panel-heading");
        widthSlider.setBlockIncrement(1);

        getChildren().addAll(heading,
                new Label("Stroke"), strokePicker,
                fillToggle, fillPicker,
                widthLabel, widthSlider);

        strokePicker.setOnAction(e -> apply());
        fillPicker.setOnAction(e -> apply());
        fillToggle.setOnAction(e -> apply());
        // Apply width once the drag ends, so a slider drag is one undo step.
        widthSlider.valueChangingProperty().addListener((o, was, now) -> {
            if (!now) {
                apply();
            }
        });
        widthSlider.valueProperty().addListener((o, a, b) -> {
            updateWidthLabel();
            if (!widthSlider.isValueChanging()) {
                apply();
            }
        });

        editor.selection().addListener(s -> loadFromSelection());
        load(editor.currentStyle());
    }

    private void apply() {
        if (syncing) {
            return;
        }
        fillPicker.setDisable(!fillToggle.isSelected());
        Style s = new Style(FxColors.fromFx(strokePicker.getValue()),
                fillToggle.isSelected() ? FxColors.fromFx(fillPicker.getValue()) : null,
                Math.round(widthSlider.getValue() * 2) / 2.0);
        editor.setStyle(s);
    }

    private void loadFromSelection() {
        List<Element> selected = editor.selection().elementsIn(editor.document());
        if (selected.size() == 1) {
            load(selected.get(0).style());
        }
    }

    private void load(Style s) {
        syncing = true;
        try {
            strokePicker.setValue(FxColors.toFx(s.stroke()));
            fillToggle.setSelected(s.fill() != null);
            if (s.fill() != null) {
                fillPicker.setValue(FxColors.toFx(s.fill()));
            }
            fillPicker.setDisable(s.fill() == null);
            widthSlider.setValue(s.strokeWidth());
            updateWidthLabel();
        } finally {
            syncing = false;
        }
    }

    private void updateWidthLabel() {
        widthLabel.setText(String.format("Width  %.1f", Math.round(widthSlider.getValue() * 2) / 2.0));
    }
}
