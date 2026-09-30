package gm.engine.dto;

/**
 * Bonus: input for creating a brand-new event from the UI. Exactly one of the LMSR-only or
 * order-book-only fields should be set, matching {@link #method}.
 */
public class CreateEventSpecDto {

    private final String name;
    private final String description;
    private final int commissionPercent;
    private final String commissionType;
    private final String option1;
    private final String option2;
    private final TradeMethod method;
    private final Integer lmsrB;
    private final Integer orderBookD;
    private final Integer orderBookInitial;
    private final Boolean orderBookAllowMint;

    public CreateEventSpecDto(String name, String description, int commissionPercent, String commissionType,
                               String option1, String option2, TradeMethod method, Integer lmsrB,
                               Integer orderBookD, Integer orderBookInitial, Boolean orderBookAllowMint) {
        this.name = name;
        this.description = description;
        this.commissionPercent = commissionPercent;
        this.commissionType = commissionType;
        this.option1 = option1;
        this.option2 = option2;
        this.method = method;
        this.lmsrB = lmsrB;
        this.orderBookD = orderBookD;
        this.orderBookInitial = orderBookInitial;
        this.orderBookAllowMint = orderBookAllowMint;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCommissionPercent() {
        return commissionPercent;
    }

    public String getCommissionType() {
        return commissionType;
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }

    public TradeMethod getMethod() {
        return method;
    }

    public Integer getLmsrB() {
        return lmsrB;
    }

    public Integer getOrderBookD() {
        return orderBookD;
    }

    public Integer getOrderBookInitial() {
        return orderBookInitial;
    }

    public Boolean getOrderBookAllowMint() {
        return orderBookAllowMint;
    }
}
