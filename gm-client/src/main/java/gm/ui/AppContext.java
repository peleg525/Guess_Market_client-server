package gm.ui;

import gm.engine.GmEngine;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.function.Consumer;

/**
 * Shared wiring every screen/component needs: the engine (now an HTTP-backed {@link GmEngine} - see
 * {@link gm.ui.net.GmHttpEngine}), the logged-in username, the animations on/off toggle, and a
 * couple of cross-screen navigation hooks set up once by {@link MarketApp}.
 * <p>
 * Exercise 2 had no real login, so an "acting as" combo box stood in for it; Exercise 3 has an
 * actual login screen (see {@link gm.ui.screen.LoginScreen}), so {@link #actingUsername()} simply
 * returns whoever is logged in for the lifetime of this client.
 */
public class AppContext {

    private final GmEngine engine;
    private final String loggedInUsername;
    private final BooleanProperty animationsEnabled = new SimpleBooleanProperty(false);

    private Consumer<Integer> openEventDetail = id -> { };
    private Runnable refreshAll = () -> { };

    public AppContext(GmEngine engine, String loggedInUsername) {
        this.engine = engine;
        this.loggedInUsername = loggedInUsername;
    }

    public GmEngine engine() {
        return engine;
    }

    public String actingUsername() {
        return loggedInUsername;
    }

    public BooleanProperty animationsEnabledProperty() {
        return animationsEnabled;
    }

    public boolean animationsEnabled() {
        return animationsEnabled.get();
    }

    public void setOpenEventDetail(Consumer<Integer> openEventDetail) {
        this.openEventDetail = openEventDetail;
    }

    public void openEventDetail(int eventId) {
        openEventDetail.accept(eventId);
    }

    public void setRefreshAll(Runnable refreshAll) {
        this.refreshAll = refreshAll;
    }

    public void refreshAll() {
        refreshAll.run();
    }
}
