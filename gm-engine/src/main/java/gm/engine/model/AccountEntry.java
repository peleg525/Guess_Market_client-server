package gm.engine.model;

/**
 * One balance-changing event on a user's account, kept for the balance-history chart and for
 * showing "why did my balance change" to the user. {@code sequence} gives a stable chart x-axis
 * independent of wall-clock resolution.
 */
public class AccountEntry {

    private final long sequence;
    private final double delta;
    private final double balanceAfter;
    private final String reason;

    public AccountEntry(long sequence, double delta, double balanceAfter, String reason) {
        this.sequence = sequence;
        this.delta = delta;
        this.balanceAfter = balanceAfter;
        this.reason = reason;
    }

    public long getSequence() {
        return sequence;
    }

    public double getDelta() {
        return delta;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public String getReason() {
        return reason;
    }
}
