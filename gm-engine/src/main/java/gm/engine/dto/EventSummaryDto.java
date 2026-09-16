package gm.engine.dto;

import gm.engine.model.EventStatus;

import java.util.List;

public class EventSummaryDto {

    private final int id;
    private final String name;
    private final String description;
    private final int commissionPercent;
    private final String commissionType;
    private final TradeMethod method;
    private final EventStatus status;
    private final String marketMakerUsername;
    private final List<String> options;

    public EventSummaryDto(int id, String name, String description, int commissionPercent, String commissionType,
                            TradeMethod method, EventStatus status, String marketMakerUsername, List<String> options) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.commissionPercent = commissionPercent;
        this.commissionType = commissionType;
        this.method = method;
        this.status = status;
        this.marketMakerUsername = marketMakerUsername;
        this.options = options;
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

    public TradeMethod getMethod() {
        return method;
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
}
