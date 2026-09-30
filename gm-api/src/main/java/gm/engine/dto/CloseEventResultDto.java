package gm.engine.dto;

public class CloseEventResultDto {

    private final EventDetailDto updatedEvent;
    private final boolean marketMakerBlocked;

    public CloseEventResultDto(EventDetailDto updatedEvent, boolean marketMakerBlocked) {
        this.updatedEvent = updatedEvent;
        this.marketMakerBlocked = marketMakerBlocked;
    }

    public EventDetailDto getUpdatedEvent() {
        return updatedEvent;
    }

    public boolean isMarketMakerBlocked() {
        return marketMakerBlocked;
    }
}
