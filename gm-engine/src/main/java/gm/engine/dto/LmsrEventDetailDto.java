package gm.engine.dto;

import gm.engine.model.EventStatus;

import java.util.List;

public class LmsrEventDetailDto extends EventDetailDto {

    private final int b;
    private final double eventAccountBalance;
    private final List<OptionStatusDto> optionStatuses;
    private final List<TradeDto> tradeHistoryNewestFirst;

    public LmsrEventDetailDto(int id, String name, String description, int commissionPercent, String commissionType,
                               EventStatus status, String marketMakerUsername, List<String> options,
                               String winningOptionName, double totalCommissionCollected, int b,
                               double eventAccountBalance, List<OptionStatusDto> optionStatuses,
                               List<TradeDto> tradeHistoryNewestFirst) {
        super(id, name, description, commissionPercent, commissionType, status, marketMakerUsername, options,
                winningOptionName, totalCommissionCollected);
        this.b = b;
        this.eventAccountBalance = eventAccountBalance;
        this.optionStatuses = optionStatuses;
        this.tradeHistoryNewestFirst = tradeHistoryNewestFirst;
    }

    public int getB() {
        return b;
    }

    public double getEventAccountBalance() {
        return eventAccountBalance;
    }

    public List<OptionStatusDto> getOptionStatuses() {
        return optionStatuses;
    }

    public List<TradeDto> getTradeHistoryNewestFirst() {
        return tradeHistoryNewestFirst;
    }
}
