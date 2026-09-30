package gm.engine.exception;

/**
 * Base class for every runtime error the engine can raise. Unchecked on purpose: the UI layer
 * catches these at natural boundaries (menu actions, background tasks) and shows a friendly
 * message, without forcing every engine call site to declare a throws clause.
 */
public abstract class GmException extends RuntimeException {

    protected GmException(String message) {
        super(message);
    }
}
