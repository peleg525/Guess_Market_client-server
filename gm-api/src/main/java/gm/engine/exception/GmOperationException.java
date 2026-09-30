package gm.engine.exception;

/**
 * Thrown when a requested operation (view an event, place an order, open/close an event, ...)
 * cannot be carried out given the engine's current state: unknown event/user, invalid
 * option/quantity/price, event already closed, insufficient funds or shares, acting user is not
 * the event's market maker, and so on.
 */
public class GmOperationException extends GmException {

    public GmOperationException(String message) {
        super(message);
    }
}
