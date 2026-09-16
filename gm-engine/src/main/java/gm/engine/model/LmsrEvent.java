package gm.engine.model;

import gm.engine.exception.GmOperationException;
import gm.engine.lmsr.LmsrCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A binary event traded through LMSR (Appendix A). The event's own account balance acts as the
 * pool: the market maker funds it with the initial subsidy on {@link #open}, every purchase adds
 * its full cost (shares + commission) into that same pool, and on {@link #close} winners are
 * paid $1 per winning share out of the pool before whatever is left over sweeps back to the
 * market maker - exactly the mechanic worked out in the appendix's numeric example.
 */
public class LmsrEvent extends Event {

    private final int b;
    private final double[] quantities;
    private double eventAccountBalance = 0.0;
    private final List<Trade> tradeHistory = new ArrayList<>();
    private final Map<String, Holding> holdings = new LinkedHashMap<>();

    public LmsrEvent(int id, String name, String description, int commissionPercent,
                      CommissionType commissionType, List<String> options, String marketMakerUsername, int b) {
        super(id, name, description, commissionPercent, commissionType, options, marketMakerUsername);
        this.b = b;
        this.quantities = new double[options.size()];
    }

    public int getB() {
        return b;
    }

    public double getEventAccountBalance() {
        return eventAccountBalance;
    }

    public double currentPrice(int optionIndex) {
        return LmsrCalculator.price(quantities, b, optionIndex);
    }

    public double totalSharesBought(int optionIndex) {
        return quantities[optionIndex];
    }

    public List<Trade> getTradeHistory() {
        return Collections.unmodifiableList(tradeHistory);
    }

    public Holding getHolding(String username) {
        return holdings.get(username);
    }

    public Map<String, Holding> getAllHoldings() {
        return Collections.unmodifiableMap(holdings);
    }

    public void open(User marketMaker, long sequence) {
        requireMarketMaker(marketMaker);
        if (getStatus() != EventStatus.NOT_STARTED) {
            throw new GmOperationException("Event '" + getName() + "' has already been opened.");
        }
        double subsidy = LmsrCalculator.cost(quantities, b);
        marketMaker.getAccount().debit(subsidy, "Opened event '" + getName() + "': paid LMSR subsidy", sequence);
        eventAccountBalance += subsidy;
        setStatus(EventStatus.ACTIVE);
    }

    public PurchaseOutcome buy(User buyer, int optionIndex, double quantity, long sequence) {
        if (getStatus() != EventStatus.ACTIVE) {
            throw new GmOperationException("Event '" + getName() + "' is not active. Trading is not possible.");
        }
        requireValidOptionIndex(optionIndex);
        if (quantity <= 0) {
            throw new GmOperationException("Quantity must be a positive number of shares.");
        }

        double before = LmsrCalculator.cost(quantities, b);
        quantities[optionIndex] += quantity;
        double after = LmsrCalculator.cost(quantities, b);
        double sharesCost = after - before;

        double commission = getCommissionType() == CommissionType.ON_PURCHASE
                ? sharesCost * getCommissionPercent() / 100.0
                : 0.0;
        double totalPaid = sharesCost + commission;

        buyer.getAccount().debit(totalPaid, "Bought " + quantity + " '" + getOptions().get(optionIndex)
                + "' shares in event '" + getName() + "'", sequence);
        eventAccountBalance += totalPaid;
        addCommissionCollected(commission);

        holdings.computeIfAbsent(buyer.getName(), n -> new Holding(getOptions().size()))
                .add(optionIndex, quantity, sharesCost);
        tradeHistory.add(new Trade(sequence, buyer.getName(), optionIndex, quantity, sharesCost, commission));

        return new PurchaseOutcome(sharesCost, commission);
    }

    public SettlementResult close(User marketMaker, int winningOptionIndex, long sequence) {
        requireMarketMaker(marketMaker);
        if (getStatus() != EventStatus.ACTIVE) {
            throw new GmOperationException("Event '" + getName() + "' is not active and cannot be closed.");
        }
        requireValidOptionIndex(winningOptionIndex);

        double netFactor = getCommissionType() == CommissionType.ON_CLOSE
                ? (1.0 - getCommissionPercent() / 100.0)
                : 1.0;

        double totalWinningShares = quantities[winningOptionIndex];
        Map<String, Double> payouts = new LinkedHashMap<>();
        for (Map.Entry<String, Holding> entry : holdings.entrySet()) {
            double winnerShares = entry.getValue().getQuantity(winningOptionIndex);
            if (winnerShares > 0) {
                payouts.put(entry.getKey(), winnerShares * netFactor);
            }
        }

        addCommissionCollected(totalWinningShares * (1.0 - netFactor));
        eventAccountBalance -= totalWinningShares; // full $1/share leaves the pool; the fee slice stays uncredited to winners

        setStatus(EventStatus.CLOSED);
        setWinningOptionIndex(winningOptionIndex);

        boolean blocked = marketMaker.getAccount().settle(eventAccountBalance,
                "Event '" + getName() + "' closed: remaining pool returned", sequence);
        eventAccountBalance = 0.0;
        return new SettlementResult(payouts, blocked);
    }

    private void requireMarketMaker(User user) {
        if (!user.getName().equals(getMarketMakerUsername())) {
            throw new GmOperationException("Only '" + getMarketMakerUsername()
                    + "' (the market maker of this event) can perform this action.");
        }
    }
}
