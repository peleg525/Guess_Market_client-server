package gm.engine.model;

/**
 * One executed fill, LMSR purchase or order-book match alike. {@code pricePaid} is the total
 * amount paid for {@code quantity} shares (not a per-share price). Also doubles as the source
 * data for the price-history chart bonus, ordered by {@code sequence}.
 */
public class Trade {

    private final long sequence;
    private final String ownerUsername;
    private final int optionIndex;
    private final double quantity;
    private final double pricePaid;
    private final double commissionPaid;

    public Trade(long sequence, String ownerUsername, int optionIndex, double quantity,
                 double pricePaid, double commissionPaid) {
        this.sequence = sequence;
        this.ownerUsername = ownerUsername;
        this.optionIndex = optionIndex;
        this.quantity = quantity;
        this.pricePaid = pricePaid;
        this.commissionPaid = commissionPaid;
    }

    public long getSequence() {
        return sequence;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public int getOptionIndex() {
        return optionIndex;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getPricePaid() {
        return pricePaid;
    }

    public double getCommissionPaid() {
        return commissionPaid;
    }
}
