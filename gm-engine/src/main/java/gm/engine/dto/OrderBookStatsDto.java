package gm.engine.dto;

/** LAST / BID / ASK / MID / SPREAD for one option. Any field can be null (no data yet). */
public class OrderBookStatsDto {

    private final Double last;
    private final Double bid;
    private final Double ask;
    private final Double mid;
    private final Double spread;

    public OrderBookStatsDto(Double last, Double bid, Double ask) {
        this.last = last;
        this.bid = bid;
        this.ask = ask;
        this.mid = (bid != null && ask != null) ? (bid + ask) / 2.0 : null;
        this.spread = (bid != null && ask != null) ? (ask - bid) : null;
    }

    public Double getLast() {
        return last;
    }

    public Double getBid() {
        return bid;
    }

    public Double getAsk() {
        return ask;
    }

    public Double getMid() {
        return mid;
    }

    public Double getSpread() {
        return spread;
    }
}
