package gm.engine.model;

/**
 * Lets an {@link Event} move money into/out of accounts it doesn't own a reference to - order
 * book counterparties and the market maker are only known by username inside the event, while
 * the actual {@link User}/{@link Account} registry lives in the engine. The engine supplies this
 * as a small adapter over its user map.
 */
public interface AccountMover {

    void credit(String username, double amount, String reason, long sequence);

    /**
     * Charges a user for a cost they had no way to see coming (order-book commission realized
     * only once their resting order gets matched). Allowed to push the balance negative.
     *
     * @return true if this charge pushed the account negative, newly blocking it.
     */
    boolean chargeLeniently(String username, double amount, String reason, long sequence);
}
