package gm.ui.util;

import java.util.Locale;

/** Every decimal amount shown to the user is formatted to exactly 2 decimal places. */
public final class Money {

    private Money() {
    }

    public static String format(double amount) {
        return String.format(Locale.ROOT, "%.2f", amount);
    }

    public static String formatShares(double quantity) {
        if (Math.abs(quantity - Math.round(quantity)) < 1e-9) {
            return String.valueOf(Math.round(quantity));
        }
        return String.format(Locale.ROOT, "%.2f", quantity);
    }
}
