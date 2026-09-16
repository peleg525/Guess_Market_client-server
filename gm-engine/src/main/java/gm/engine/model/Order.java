package gm.engine.model;

/**
 * A resting or partially-filled order-book entry. {@code sequence} gives price-time priority
 * (lower sequence = placed earlier) once two orders share a price level.
 */
public class Order {

    private final long sequence;
    private final String ownerUsername;
    private final int optionIndex;
    private final OrderSide side;
    private final double price;
    private double remainingQuantity;

    public Order(long sequence, String ownerUsername, int optionIndex, OrderSide side,
                 double price, double quantity) {
        this.sequence = sequence;
        this.ownerUsername = ownerUsername;
        this.optionIndex = optionIndex;
        this.side = side;
        this.price = price;
        this.remainingQuantity = quantity;
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

    public OrderSide getSide() {
        return side;
    }

    public double getPrice() {
        return price;
    }

    public double getRemainingQuantity() {
        return remainingQuantity;
    }

    public void reduceBy(double quantity) {
        remainingQuantity -= quantity;
    }

    public boolean isFullyFilled() {
        return remainingQuantity <= 1e-9;
    }
}
