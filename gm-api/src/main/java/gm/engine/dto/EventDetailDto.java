package gm.engine.dto;

import gm.engine.model.EventStatus;

import java.util.List;

/**
 * Common fields for an event's full detail view. The UI switches on the concrete subclass
 * ({@link LmsrEventDetailDto} or {@link OrderBookEventDetailDto}) to render the method-specific
 * trading panel, the same way the engine's own {@code Event} hierarchy splits by method.
 */
public abstract class EventDetailDto {

    private final int id;
    private final String name;
    private final String description;
    private final int commissionPercent;
    private final String commissionType;
    private final EventStatus status;
    private final String marketMakerUsername;
    private final List<String> options;
    private final String winningOptionName;
    private final double totalCommissionCollected;

    protected EventDetailDto(int id, String name, String description, int commissionPercent, String commissionType,
                              EventStatus status, String marketMakerUsername, List<String> options,
                              String winningOptionName, double totalCommissionCollected) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.commissionPercent = commissionPercent;
        this.commissionType = commissionType;
        this.status = status;
        this.marketMakerUsername = marketMakerUsername;
        this.options = options;
        this.winningOptionName = winningOptionName;
        this.totalCommissionCollected = totalCommissionCollected;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCommissionPercent() {
        return commissionPercent;
    }

    public String getCommissionType() {
        return commissionType;
    }

    public EventStatus getStatus() {
        return status;
    }

    public String getMarketMakerUsername() {
        return marketMakerUsername;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getWinningOptionName() {
        return winningOptionName;
    }

    public double getTotalCommissionCollected() {
        return totalCommissionCollected;
    }
}
