// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

import java.util.Objects;
import java.util.Optional;

/**
 * How an element is painted.
 *
 * @param stroke      outline colour
 * @param fill        interior colour, or {@code null} for no fill
 * @param strokeWidth outline width in document units, at least 0.5
 */
public record Style(Rgba stroke, Rgba fill, double strokeWidth) {

    public static final Style DEFAULT = new Style(Rgba.BLACK, null, 3);

    public Style {
        Objects.requireNonNull(stroke, "stroke");
        if (!(strokeWidth >= 0.5)) {
            throw new IllegalArgumentException("stroke width must be >= 0.5");
        }
    }

    public Optional<Rgba> fillColor() {
        return Optional.ofNullable(fill);
    }

    public Style withStroke(Rgba c) {
        return new Style(c, fill, strokeWidth);
    }

    public Style withFill(Rgba c) {
        return new Style(stroke, c, strokeWidth);
    }

    public Style withStrokeWidth(double w) {
        return new Style(stroke, fill, w);
    }
}
