// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.model;

import java.util.Locale;

/**
 * A colour with components in [0, 1]. The model has its own colour type so it
 * stays free of JavaFX and can be unit-tested headlessly.
 */
public record Rgba(double r, double g, double b, double a) {

    public static final Rgba BLACK = new Rgba(0, 0, 0, 1);
    public static final Rgba WHITE = new Rgba(1, 1, 1, 1);
    public static final Rgba TRANSPARENT = new Rgba(0, 0, 0, 0);

    public Rgba {
        r = clamp(r);
        g = clamp(g);
        b = clamp(b);
        a = clamp(a);
    }

    private static double clamp(double v) {
        return Math.max(0, Math.min(1, v));
    }

    /** Parses #RRGGBB or #RRGGBBAA. */
    public static Rgba fromHex(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        if (h.length() != 6 && h.length() != 8) {
            throw new IllegalArgumentException("bad colour: " + hex);
        }
        try {
            int r = Integer.parseInt(h.substring(0, 2), 16);
            int g = Integer.parseInt(h.substring(2, 4), 16);
            int b = Integer.parseInt(h.substring(4, 6), 16);
            int a = h.length() == 8 ? Integer.parseInt(h.substring(6, 8), 16) : 255;
            return new Rgba(r / 255.0, g / 255.0, b / 255.0, a / 255.0);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("bad colour: " + hex, e);
        }
    }

    /** Formats as #RRGGBBAA. */
    public String toHex() {
        return String.format(Locale.ROOT, "#%02x%02x%02x%02x",
                byteOf(r), byteOf(g), byteOf(b), byteOf(a));
    }

    /** Formats as #RRGGBB, ignoring alpha (for SVG fill/stroke attributes). */
    public String toHexRgb() {
        return String.format(Locale.ROOT, "#%02x%02x%02x", byteOf(r), byteOf(g), byteOf(b));
    }

    private static int byteOf(double v) {
        return (int) Math.round(v * 255);
    }
}
