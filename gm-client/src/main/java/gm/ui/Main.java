package gm.ui;

import javafx.application.Application;

/**
 * Kept separate from {@link MarketApp} (which extends {@code Application}) on purpose: launching
 * a plain jar with an {@code Application} subclass as the manifest's main class is the classic
 * cause of "missing JavaFX runtime components" when there's no module-path set up, which is
 * exactly how this app is expected to run (batch file, no IDE). Calling {@code launch} from a
 * plain class avoids that.
 */
public class Main {

    public static void main(String[] args) {
        Application.launch(MarketApp.class, args);
    }
}
