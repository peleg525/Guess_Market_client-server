package gm.engine.dto;

public class BalancePointDto {

    private final long sequence;
    private final double balance;
    private final String reason;

    public BalancePointDto(long sequence, double balance, String reason) {
        this.sequence = sequence;
        this.balance = balance;
        this.reason = reason;
    }

    public long getSequence() {
        return sequence;
    }

    public double getBalance() {
        return balance;
    }

    public String getReason() {
        return reason;
    }
}
