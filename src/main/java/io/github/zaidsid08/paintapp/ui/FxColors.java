// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.ui;

import io.github.zaidsid08.paintapp.model.Rgba;
import javafx.scene.paint.Color;

/** Converts between the model's colour type and JavaFX's. */
final class FxColors {

    private FxColors() {
    }

    static Color toFx(Rgba c) {
        return new Color(c.r(), c.g(), c.b(), c.a());
    }

    static Rgba fromFx(Color c) {
        return new Rgba(c.getRed(), c.getGreen(), c.getBlue(), c.getOpacity());
    }
}
