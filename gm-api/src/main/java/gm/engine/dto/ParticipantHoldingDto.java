package gm.engine.dto;

import java.util.List;

public class ParticipantHoldingDto {

    private final String username;
    private final List<Double> quantityPerOption;
    private final List<Double> amountPaidPerOption;

    public ParticipantHoldingDto(String username, List<Double> quantityPerOption, List<Double> amountPaidPerOption) {
        this.username = username;
        this.quantityPerOption = quantityPerOption;
        this.amountPaidPerOption = amountPaidPerOption;
    }

    public String getUsername() {
        return username;
    }

    public List<Double> getQuantityPerOption() {
        return quantityPerOption;
    }

    public List<Double> getAmountPaidPerOption() {
        return amountPaidPerOption;
    }
}
