package gm.ui.util;

import javafx.application.Platform;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Runs one blocking call (every {@code GmEngine} call is now a blocking HTTP request instead of a
 * local, effectively-instant method call) off the JavaFX Application Thread, then hands the result
 * back on it. Every button handler that used to call the engine directly and update the UI in the
 * same breath now goes through here instead, so a slow/unreachable server never freezes the window.
 */
public final class Async {

    private static final ExecutorService POOL = Executors.newCachedThreadPool(daemonThreadFactory());

    public static <T> void call(Supplier<T> action, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        POOL.submit(() -> {
            try {
                T result = action.get();
                Platform.runLater(() -> onSuccess.accept(result));
            } catch (Throwable t) {
                Platform.runLater(() -> onError.accept(t));
            }
        });
    }

    public static void run(Runnable action, Runnable onSuccess, Consumer<Throwable> onError) {
        call(() -> {
            action.run();
            return null;
        }, ignored -> onSuccess.run(), onError);
    }

    private static ThreadFactory daemonThreadFactory() {
        return runnable -> {
            Thread thread = new Thread(runnable, "gm-async");
            thread.setDaemon(true);
            return thread;
        };
    }

    private Async() {
    }
}
