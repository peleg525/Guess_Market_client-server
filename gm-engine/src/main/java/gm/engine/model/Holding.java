package gm.engine.model;

/**
 * One user's position within a single event: shares held and net cash paid, per option index.
 * Used to pay out winners pro-rata at close and to show the "participants" panel for order-book
 * events.
 */
public class Holding {

    private final double[] quantities;
    private final double[] amountPaid;

    public Holding(int optionCount) {
        this.quantities = new double[optionCount];
        this.amountPaid = new double[optionCount];
    }

    public double getQuantity(int optionIndex) {
        return quantities[optionIndex];
    }

    public double getAmountPaid(int optionIndex) {
        return amountPaid[optionIndex];
    }

    public void add(int optionIndex, double quantity, double paid) {
        quantities[optionIndex] += quantity;
        amountPaid[optionIndex] += paid;
    }

    public boolean hasAnyPosition() {
        for (double q : quantities) {
            if (q != 0) {
                return true;
            }
        }
        return false;
    }
}
