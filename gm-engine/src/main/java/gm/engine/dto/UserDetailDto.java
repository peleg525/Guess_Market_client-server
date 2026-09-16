package gm.engine.dto;

import java.util.List;

public class UserDetailDto {

    private final String name;
    private final double balance;
    private final boolean blocked;
    private final List<ParticipationSummaryDto> participations;

    public UserDetailDto(String name, double balance, boolean blocked, List<ParticipationSummaryDto> participations) {
        this.name = name;
        this.balance = balance;
        this.blocked = blocked;
        this.participations = participations;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public List<ParticipationSummaryDto> getParticipations() {
        return participations;
    }
}
