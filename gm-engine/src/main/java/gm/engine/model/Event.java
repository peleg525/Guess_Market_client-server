package gm.engine.model;

import java.util.List;

/**
 * Common state for both trading methods: identity, the two binary options, commission rules and
 * lifecycle. Trading mechanics differ enough between LMSR and the order book (different
 * parameters, different settlement math) that they live entirely in the two subclasses rather
 * than behind a shared "trade" method here.
 */
public abstract class Event {

    private final int id;
    private final String name;
    private final String description;
    private final int commissionPercent;
    private final CommissionType commissionType;
    private final List<String> options;
    private final String marketMakerUsername;

    private EventStatus status = EventStatus.NOT_STARTED;
    private Integer winningOptionIndex = null;
    private double totalCommissionCollected = 0.0;

    protected Event(int id, String name, String description, int commissionPercent,
                     CommissionType commissionType, List<String> options, String marketMakerUsername) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.commissionPercent = commissionPercent;
        this.commissionType = commissionType;
        this.options = List.copyOf(options);
        this.marketMakerUsername = marketMakerUsername;
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

    public CommissionType getCommissionType() {
        return commissionType;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getMarketMakerUsername() {
        return marketMakerUsername;
    }

    public EventStatus getStatus() {
        return status;
    }

    protected void setStatus(EventStatus status) {
        this.status = status;
    }

    public Integer getWinningOptionIndex() {
        return winningOptionIndex;
    }

    protected void setWinningOptionIndex(Integer winningOptionIndex) {
        this.winningOptionIndex = winningOptionIndex;
    }

    public double getTotalCommissionCollected() {
        return totalCommissionCollected;
    }

    protected void addCommissionCollected(double amount) {
        totalCommissionCollected += amount;
    }

    public int otherOption(int optionIndex) {
        return optionIndex == 0 ? 1 : 0;
    }

    protected void requireValidOptionIndex(int optionIndex) {
        if (optionIndex < 0 || optionIndex >= options.size()) {
            throw new gm.engine.exception.GmOperationException("Invalid option number for event '" + name
                    + "'. Choose a number between 1 and " + options.size() + ".");
        }
    }
}
