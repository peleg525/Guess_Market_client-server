package gm.engine.dto;

public class UserSummaryDto {

    private final String name;
    private final double balance;
    private final boolean blocked;
    private final boolean marketMakerOfAnyEvent;

    public UserSummaryDto(String name, double balance, boolean blocked, boolean marketMakerOfAnyEvent) {
        this.name = name;
        this.balance = balance;
        this.blocked = blocked;
        this.marketMakerOfAnyEvent = marketMakerOfAnyEvent;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isMarketMakerOfAnyEvent() {
        return marketMakerOfAnyEvent;
    }
}
