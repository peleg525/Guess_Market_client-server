package gm.engine.dto;

import java.util.List;

public class OptionBookDto {

    private final String optionName;
    private final List<OrderRowDto> bids;
    private final List<OrderRowDto> asks;
    private final OrderBookStatsDto stats;

    public OptionBookDto(String optionName, List<OrderRowDto> bids, List<OrderRowDto> asks, OrderBookStatsDto stats) {
        this.optionName = optionName;
        this.bids = bids;
        this.asks = asks;
        this.stats = stats;
    }

    public String getOptionName() {
        return optionName;
    }

    public List<OrderRowDto> getBids() {
        return bids;
    }

    public List<OrderRowDto> getAsks() {
        return asks;
    }

    public OrderBookStatsDto getStats() {
        return stats;
    }
}
