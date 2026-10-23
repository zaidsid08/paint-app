// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp;

import io.github.zaidsid08.paintapp.ui.PaintApp;
import javafx.application.Application;

/**
 * Plain main class. Launching through a class that doesn't extend
 * Application lets the app run from a regular classpath (e.g. a fat jar).
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Application.launch(PaintApp.class, args);
    }
}
