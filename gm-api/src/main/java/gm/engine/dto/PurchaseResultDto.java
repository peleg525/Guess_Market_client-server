package gm.engine.dto;

public class PurchaseResultDto {

    private final double sharesCost;
    private final double commissionPaid;
    private final LmsrEventDetailDto updatedEvent;

    public PurchaseResultDto(double sharesCost, double commissionPaid, LmsrEventDetailDto updatedEvent) {
        this.sharesCost = sharesCost;
        this.commissionPaid = commissionPaid;
        this.updatedEvent = updatedEvent;
    }

    public double getSharesCost() {
        return sharesCost;
    }

    public double getCommissionPaid() {
        return commissionPaid;
    }

    public double getTotalPaid() {
        return sharesCost + commissionPaid;
    }

    public LmsrEventDetailDto getUpdatedEvent() {
        return updatedEvent;
    }
}
