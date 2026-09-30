package gm.engine.dto;

public class PricePointDto {

    private final long sequence;
    private final double price;

    public PricePointDto(long sequence, double price) {
        this.sequence = sequence;
        this.price = price;
    }

    public long getSequence() {
        return sequence;
    }

    public double getPrice() {
        return price;
    }
}
