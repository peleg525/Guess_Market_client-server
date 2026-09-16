package gm.engine.dto;

import gm.engine.model.EventStatus;

import java.util.List;

public class OrderBookEventDetailDto extends EventDetailDto {

    private final int d;
    private final int initial;
    private final boolean allowMint;
    private final double eventAccountBalance;
    private final List<OptionBookDto> optionBooks;
    private final List<ParticipantHoldingDto> participants;

    public OrderBookEventDetailDto(int id, String name, String description, int commissionPercent,
                                    String commissionType, EventStatus status, String marketMakerUsername,
                                    List<String> options, String winningOptionName, double totalCommissionCollected,
                                    int d, int initial, boolean allowMint, double eventAccountBalance,
                                    List<OptionBookDto> optionBooks, List<ParticipantHoldingDto> participants) {
        super(id, name, description, commissionPercent, commissionType, status, marketMakerUsername, options,
                winningOptionName, totalCommissionCollected);
        this.d = d;
        this.initial = initial;
        this.allowMint = allowMint;
        this.eventAccountBalance = eventAccountBalance;
        this.optionBooks = optionBooks;
        this.participants = participants;
    }

    public int getD() {
        return d;
    }

    public int getInitial() {
        return initial;
    }

    public boolean isAllowMint() {
        return allowMint;
    }

    public double getEventAccountBalance() {
        return eventAccountBalance;
    }

    public List<OptionBookDto> getOptionBooks() {
        return optionBooks;
    }

    public List<ParticipantHoldingDto> getParticipants() {
        return participants;
    }
}
