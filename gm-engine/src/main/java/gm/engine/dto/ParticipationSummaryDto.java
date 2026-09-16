package gm.engine.dto;

import gm.engine.model.EventStatus;

public class ParticipationSummaryDto {

    private final int eventId;
    private final String eventName;
    private final TradeMethod method;
    private final EventStatus status;
    private final boolean marketMaker;

    public ParticipationSummaryDto(int eventId, String eventName, TradeMethod method, EventStatus status,
                                    boolean marketMaker) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.method = method;
        this.status = status;
        this.marketMaker = marketMaker;
    }

    public int getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public TradeMethod getMethod() {
        return method;
    }

    public EventStatus getStatus() {
        return status;
    }

    public boolean isMarketMaker() {
        return marketMaker;
    }
}
