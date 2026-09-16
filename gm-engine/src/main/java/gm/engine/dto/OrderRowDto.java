package gm.engine.dto;

public class OrderRowDto {

    private final String ownerUsername;
    private final double quantity;
    private final double price;

    public OrderRowDto(String ownerUsername, double quantity, double price) {
        this.ownerUsername = ownerUsername;
        this.quantity = quantity;
        this.price = price;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }
}
