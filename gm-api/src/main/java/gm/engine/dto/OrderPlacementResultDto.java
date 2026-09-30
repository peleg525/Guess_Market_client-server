package gm.engine.dto;

import java.util.List;

public class OrderPlacementResultDto {

    private final double quantityFilled;
    private final double quantityResting;
    private final List<String> newlyBlockedUsernames;
    private final OrderBookEventDetailDto updatedEvent;

    public OrderPlacementResultDto(double quantityFilled, double quantityResting, List<String> newlyBlockedUsernames,
                                    OrderBookEventDetailDto updatedEvent) {
        this.quantityFilled = quantityFilled;
        this.quantityResting = quantityResting;
        this.newlyBlockedUsernames = newlyBlockedUsernames;
        this.updatedEvent = updatedEvent;
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

    public OrderBookEventDetailDto getUpdatedEvent() {
        return updatedEvent;
    }
}
