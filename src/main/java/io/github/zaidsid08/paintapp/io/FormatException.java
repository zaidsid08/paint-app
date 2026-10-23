// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.io;

import java.io.IOException;

/** A drawing file that can't be parsed. Carries the 1-based line number. */
public final class FormatException extends IOException {

    private static final long serialVersionUID = 1L;

    private final int line;

    public FormatException(int line, String message) {
        super("line " + line + ": " + message);
        this.line = line;
    }

    public int line() {
        return line;
    }
}
