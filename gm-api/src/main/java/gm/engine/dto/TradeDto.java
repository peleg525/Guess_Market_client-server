package gm.engine.dto;

public class TradeDto {

    private final long sequence;
    private final String ownerUsername;
    private final String optionName;
    private final double quantity;
    private final double pricePaid;
    private final double commissionPaid;

    public TradeDto(long sequence, String ownerUsername, String optionName, double quantity,
                     double pricePaid, double commissionPaid) {
        this.sequence = sequence;
        this.ownerUsername = ownerUsername;
        this.optionName = optionName;
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

    public String getOptionName() {
        return optionName;
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
