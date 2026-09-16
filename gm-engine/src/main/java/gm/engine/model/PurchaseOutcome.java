package gm.engine.model;

/** Result of one LMSR purchase: how much went to the shares themselves vs. to commission. */
public class PurchaseOutcome {

    private final double sharesCost;
    private final double commissionPaid;

    public PurchaseOutcome(double sharesCost, double commissionPaid) {
        this.sharesCost = sharesCost;
        this.commissionPaid = commissionPaid;
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
}
