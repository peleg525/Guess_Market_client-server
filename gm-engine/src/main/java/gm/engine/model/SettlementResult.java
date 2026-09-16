package gm.engine.model;

import java.util.Map;

/**
 * What an event's {@code close} produced: how much cash each winning holder is owed (the event
 * itself only tracks share holdings, not other users' {@link Account} objects, so crediting them
 * is left to the engine, which owns the {@link User} registry) and whether settling the market
 * maker's own account pushed it negative.
 */
public class SettlementResult {

    private final Map<String, Double> winnerPayouts;
    private final boolean marketMakerBlocked;

    public SettlementResult(Map<String, Double> winnerPayouts, boolean marketMakerBlocked) {
        this.winnerPayouts = winnerPayouts;
        this.marketMakerBlocked = marketMakerBlocked;
    }

    public Map<String, Double> getWinnerPayouts() {
        return winnerPayouts;
    }

    public boolean isMarketMakerBlocked() {
        return marketMakerBlocked;
    }
}
