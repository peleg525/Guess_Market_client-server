package gm.engine.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class User {

    private final String name;
    private final Account account;
    private final Set<Integer> marketMakerOfEventIds = new LinkedHashSet<>();

    public User(String name, double initialCash) {
        this.name = name;
        this.account = new Account(initialCash);
    }

    public String getName() {
        return name;
    }

    public Account getAccount() {
        return account;
    }

    public void addMarketMakerOf(int eventId) {
        marketMakerOfEventIds.add(eventId);
    }

    public boolean isMarketMakerOf(int eventId) {
        return marketMakerOfEventIds.contains(eventId);
    }

    public Set<Integer> getMarketMakerOfEventIds() {
        return marketMakerOfEventIds;
    }
}
