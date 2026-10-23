// Paint App, written October 2026 by Zaid Siddiqui.
// From-scratch rewrite of coursework originally done Oct–Nov 2024. See README.

package io.github.zaidsid08.paintapp.ui;

import javafx.application.Application;
import javafx.stage.Stage;

/** JavaFX entry point. */
public final class PaintApp extends Application {

    @Override
    public void start(Stage stage) {
        new MainWindow(stage).show();
    }
}
