package gm.engine.model;

import java.util.List;

/** Outcome of placing one order-book order: how much filled immediately, and who got blocked. */
public class OrderPlacementResult {

    private final double quantityFilled;
    private final double quantityResting;
    private final List<String> newlyBlockedUsernames;

    public OrderPlacementResult(double quantityFilled, double quantityResting, List<String> newlyBlockedUsernames) {
        this.quantityFilled = quantityFilled;
        this.quantityResting = quantityResting;
        this.newlyBlockedUsernames = newlyBlockedUsernames;
    }

    public double getQuantityFilled() {
        return quantityFilled;
    }

    public double getQuantityResting() {
        return quantityResting;
    }

    public List<String> getNewlyBlockedUsernames() {
        return newlyBlockedUsernames;
    }
}
