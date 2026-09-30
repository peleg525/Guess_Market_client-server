package gm.engine.dto;

public class OptionStatusDto {

    private final String optionName;
    private final double currentPrice;
    private final double totalSharesBought;

    public OptionStatusDto(String optionName, double currentPrice, double totalSharesBought) {
        this.optionName = optionName;
        this.currentPrice = currentPrice;
        this.totalSharesBought = totalSharesBought;
    }

    public String getOptionName() {
        return optionName;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getTotalSharesBought() {
        return totalSharesBought;
    }
}
