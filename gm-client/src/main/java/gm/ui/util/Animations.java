package gm.ui.util;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Bonus: short (well under the 2s cap) animations, applied only when the caller's "animations
 * enabled" flag is on - otherwise every method here is a no-op, so disabling the toggle doesn't
 * just speed animations up, it removes them entirely.
 */
public final class Animations {

    private Animations() {
    }

    public static void fadeIn(Node node, boolean enabled) {
        if (!enabled) {
            return;
        }
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(350), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    public static void pulse(Node node, boolean enabled) {
        if (!enabled) {
            return;
        }
        ScaleTransition scale = new ScaleTransition(Duration.millis(180), node);
        scale.setFromX(1);
        scale.setFromY(1);
        scale.setToX(1.06);
        scale.setToY(1.06);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }
}
