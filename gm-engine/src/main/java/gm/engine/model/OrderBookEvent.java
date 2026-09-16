package gm.engine.model;

import gm.engine.exception.GmOperationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A binary event traded through a central limit order book (Appendix B). Matching follows
 * price-then-time priority: an incoming order first walks the resting opposite-side orders on
 * the same option (best price first), then - if it's a BUY and minting is allowed - looks for a
 * resting BUY on the complementary option whose price plus its own sums to at least {@code d},
 * minting new pairs so both sides get their shares and the combined payment lands in the event's
 * own collateral account. This mirrors the lecturer's reference simulation
 * (targil01/ex2/clob_simulation.html) fill for fill.
 * <p>
 * Funds/shares for a new order are reserved in full at placement time (there is no order-cancel
 * feature in this exercise, so a resting order's reservation never needs to be given back).
 * Commission, whose exact total can't be known until an order actually executes, is instead
 * charged leniently at match time via {@link AccountMover#chargeLeniently} - which can push a
 * balance negative and block the account, matching the assignment's settlement rule.
 */
public class OrderBookEvent extends Event {

    private static final double PRICE_EPSILON = 1e-9;

    private final int d;
    private final int initial;
    private final boolean allowMint;

    private final List<List<Order>> bids = new ArrayList<>();
    private final List<List<Order>> asks = new ArrayList<>();
    private final Double[] lastTradePrice;
    private final Map<String, Holding> holdings = new LinkedHashMap<>();
    private final List<Trade> tradeHistory = new ArrayList<>();

    private double eventAccountBalance = 0.0;

    public OrderBookEvent(int id, String name, String description, int commissionPercent,
                           CommissionType commissionType, List<String> options, String marketMakerUsername,
                           int d, int initial, boolean allowMint) {
        super(id, name, description, commissionPercent, commissionType, options, marketMakerUsername);
        this.d = d;
        this.initial = initial;
        this.allowMint = allowMint;
        this.lastTradePrice = new Double[options.size()];
        for (int i = 0; i < options.size(); i++) {
            bids.add(new ArrayList<>());
            asks.add(new ArrayList<>());
        }
    }

    public int getD() {
        return d;
    }

    public int getInitial() {
        return initial;
    }

    public boolean isAllowMint() {
        return allowMint;
    }

    public double getEventAccountBalance() {
        return eventAccountBalance;
    }

    public List<Order> getBids(int optionIndex) {
        return Collections.unmodifiableList(bids.get(optionIndex));
    }

    public List<Order> getAsks(int optionIndex) {
        return Collections.unmodifiableList(asks.get(optionIndex));
    }

    public Double getLastTradePrice(int optionIndex) {
        return lastTradePrice[optionIndex];
    }

    public Double getBestBid(int optionIndex) {
        List<Order> b = bids.get(optionIndex);
        return b.isEmpty() ? null : b.get(0).getPrice();
    }

    public Double getBestAsk(int optionIndex) {
        List<Order> a = asks.get(optionIndex);
        return a.isEmpty() ? null : a.get(0).getPrice();
    }

    public Holding getHolding(String username) {
        return holdings.get(username);
    }

    public Map<String, Holding> getAllHoldings() {
        return Collections.unmodifiableMap(holdings);
    }

    public List<Trade> getTradeHistory() {
        return Collections.unmodifiableList(tradeHistory);
    }

    public void open(User marketMaker, long sequence) {
        requireMarketMaker(marketMaker);
        if (getStatus() != EventStatus.NOT_STARTED) {
            throw new GmOperationException("Event '" + getName() + "' has already been opened.");
        }
        if (initial > 0) {
            marketMaker.getAccount().debit(initial, "Opened event '" + getName() + "': initial share purchase", sequence);
        }
        eventAccountBalance += initial;
        double pairs = initial / (double) d;
        Holding holding = holdingOf(marketMaker.getName());
        holding.add(0, pairs, initial / 2.0);
        holding.add(1, pairs, initial / 2.0);
        setStatus(EventStatus.ACTIVE);
    }

    public OrderPlacementResult placeOrder(User trader, int optionIndex, OrderSide side, double quantity,
                                            double price, long sequence, AccountMover mover) {
        if (getStatus() != EventStatus.ACTIVE) {
            throw new GmOperationException("Event '" + getName() + "' is not active. Trading is not possible.");
        }
        requireValidOptionIndex(optionIndex);
        if (quantity <= 0) {
            throw new GmOperationException("Quantity must be a positive number of shares.");
        }
        double maxPrice = d - 0.01;
        if (price <= 0 || price > maxPrice + PRICE_EPSILON) {
            throw new GmOperationException("Price must be between 0.01 and " + maxPrice
                    + " (the event's base value is " + d + ").");
        }

        if (side == OrderSide.SELL) {
            Holding holding = holdingOf(trader.getName());
            if (holding.getQuantity(optionIndex) + PRICE_EPSILON < quantity) {
                throw new GmOperationException("Insufficient shares: you hold " + holding.getQuantity(optionIndex)
                        + " but tried to sell " + quantity + ".");
            }
            holding.add(optionIndex, -quantity, 0);
        } else {
            trader.getAccount().debit(quantity * price, "Reserved cash for a buy order in event '" + getName() + "'", sequence);
        }

        List<String> blocked = new ArrayList<>();
        double remaining = quantity;

        remaining = matchSameOption(trader, optionIndex, side, price, remaining, sequence, mover, blocked);
        if (side == OrderSide.BUY && remaining > 0 && allowMint) {
            remaining = matchMint(trader, optionIndex, price, remaining, sequence, mover, blocked);
        }

        double resting = remaining;
        if (remaining > PRICE_EPSILON) {
            Order order = new Order(sequence, trader.getName(), optionIndex, side, price, remaining);
            List<Order> book = side == OrderSide.BUY ? bids.get(optionIndex) : asks.get(optionIndex);
            insertSorted(book, order, side == OrderSide.BUY);
        } else {
            resting = 0;
        }

        return new OrderPlacementResult(quantity - resting, resting, blocked);
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

        Map<String, Double> payouts = new LinkedHashMap<>();
        for (Map.Entry<String, Holding> entry : holdings.entrySet()) {
            double winnerShares = entry.getValue().getQuantity(winningOptionIndex);
            if (winnerShares > 0) {
                double gross = winnerShares * d;
                double net = gross * netFactor;
                payouts.put(entry.getKey(), net);
                addCommissionCollected(gross - net);
                eventAccountBalance -= net;
            }
        }

        setStatus(EventStatus.CLOSED);
        setWinningOptionIndex(winningOptionIndex);

        boolean blocked = marketMaker.getAccount().settle(eventAccountBalance,
                "Event '" + getName() + "' closed: remaining pool (incl. commission) returned", sequence);
        eventAccountBalance = 0.0;
        return new SettlementResult(payouts, blocked);
    }

    // ---- matching ----

    private double matchSameOption(User trader, int optionIndex, OrderSide side, double price, double remaining,
                                    long sequence, AccountMover mover, List<String> blocked) {
        List<Order> opposingBook = side == OrderSide.BUY ? asks.get(optionIndex) : bids.get(optionIndex);

        while (remaining > PRICE_EPSILON && !opposingBook.isEmpty()) {
            Order resting = opposingBook.get(0);
            boolean crosses = side == OrderSide.BUY ? resting.getPrice() <= price + PRICE_EPSILON
                    : resting.getPrice() >= price - PRICE_EPSILON;
            if (!crosses) {
                break;
            }

            double fillQty = Math.min(remaining, resting.getRemainingQuantity());
            double fillPrice = resting.getPrice();
            double baseAmount = fillQty * fillPrice;

            if (side == OrderSide.BUY) {
                holdingOf(trader.getName()).add(optionIndex, fillQty, baseAmount);
                double refund = (price - fillPrice) * fillQty;
                if (refund > 0) {
                    trader.getAccount().credit(refund, "Filled at a better price than your limit", sequence);
                }
                mover.credit(resting.getOwnerUsername(), baseAmount, "Sold " + fillQty + " shares in event '"
                        + getName() + "'", sequence);
                chargeOnPurchaseCommission(trader, trader.getName(), baseAmount, sequence, mover, blocked, true);
                recordTrade(sequence, trader.getName(), optionIndex, fillQty, baseAmount);
            } else {
                holdingOf(resting.getOwnerUsername()).add(optionIndex, fillQty, baseAmount);
                trader.getAccount().credit(baseAmount, "Sold " + fillQty + " shares in event '" + getName() + "'", sequence);
                chargeOnPurchaseCommission(null, resting.getOwnerUsername(), baseAmount, sequence, mover, blocked, false);
                recordTrade(sequence, resting.getOwnerUsername(), optionIndex, fillQty, baseAmount);
            }

            lastTradePrice[optionIndex] = fillPrice;
            resting.reduceBy(fillQty);
            remaining -= fillQty;
            if (resting.isFullyFilled()) {
                opposingBook.remove(0);
            }
        }
        return remaining;
    }

    private double matchMint(User trader, int optionIndex, double price, double remaining, long sequence,
                              AccountMover mover, List<String> blocked) {
        int other = otherOption(optionIndex);
        List<Order> complementaryBids = bids.get(other);
        List<Order> sortedByBestPrice = new ArrayList<>(complementaryBids);
        sortedByBestPrice.sort(Comparator.comparingDouble(Order::getPrice).reversed()
                .thenComparingLong(Order::getSequence));

        for (Order resting : sortedByBestPrice) {
            if (remaining <= PRICE_EPSILON) {
                break;
            }
            if (resting.getPrice() + price < d - PRICE_EPSILON) {
                // sorted best-price-first: once one resting bid falls short, lower ones will too
                break;
            }

            double mintQty = Math.min(remaining, resting.getRemainingQuantity());
            double restingPays = resting.getPrice();
            double traderPays = d - restingPays;

            holdingOf(resting.getOwnerUsername()).add(other, mintQty, mintQty * restingPays);
            holdingOf(trader.getName()).add(optionIndex, mintQty, mintQty * traderPays);
            eventAccountBalance += mintQty * d;

            double refund = (price - traderPays) * mintQty;
            if (refund > 0) {
                trader.getAccount().credit(refund, "Minted at a better price than your limit", sequence);
            }

            chargeOnPurchaseCommission(trader, trader.getName(), mintQty * traderPays, sequence, mover, blocked, true);
            chargeOnPurchaseCommission(null, resting.getOwnerUsername(), mintQty * restingPays, sequence, mover, blocked, false);

            lastTradePrice[optionIndex] = traderPays;
            lastTradePrice[other] = restingPays;

            recordTrade(sequence, trader.getName(), optionIndex, mintQty, mintQty * traderPays);
            recordTrade(sequence, resting.getOwnerUsername(), other, mintQty, mintQty * restingPays);

            resting.reduceBy(mintQty);
            if (resting.isFullyFilled()) {
                complementaryBids.remove(resting);
            }
            remaining -= mintQty;
        }
        return remaining;
    }

    private void chargeOnPurchaseCommission(User directTrader, String buyerUsername, double baseAmount,
                                             long sequence, AccountMover mover, List<String> blocked,
                                             boolean buyerIsDirectTrader) {
        if (getCommissionType() != CommissionType.ON_PURCHASE) {
            return;
        }
        double commission = baseAmount * getCommissionPercent() / 100.0;
        addCommissionCollected(commission);
        mover.credit(getMarketMakerUsername(), commission, "Commission from event '" + getName() + "'", sequence);
        boolean newlyBlocked = buyerIsDirectTrader
                ? directTrader.getAccount().settle(-commission, "Commission on a fill in event '" + getName() + "'", sequence)
                : mover.chargeLeniently(buyerUsername, commission, "Commission on a fill in event '" + getName() + "'", sequence);
        if (newlyBlocked) {
            blocked.add(buyerUsername);
        }
    }

    private void recordTrade(long sequence, String buyerUsername, int optionIndex, double quantity, double amount) {
        tradeHistory.add(new Trade(sequence, buyerUsername, optionIndex, quantity, amount, 0));
    }

    private Holding holdingOf(String username) {
        return holdings.computeIfAbsent(username, n -> new Holding(getOptions().size()));
    }

    private void insertSorted(List<Order> book, Order order, boolean descendingPrice) {
        int index = 0;
        while (index < book.size()) {
            Order existing = book.get(index);
            boolean ordersBefore = descendingPrice
                    ? existing.getPrice() > order.getPrice() + PRICE_EPSILON
                    : existing.getPrice() < order.getPrice() - PRICE_EPSILON;
            if (ordersBefore) {
                index++;
            } else {
                break;
            }
        }
        book.add(index, order);
    }

    private void requireMarketMaker(User user) {
        if (!user.getName().equals(getMarketMakerUsername())) {
            throw new GmOperationException("Only '" + getMarketMakerUsername()
                    + "' (the market maker of this event) can perform this action.");
        }
    }
}
