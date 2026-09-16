package gm.engine;

import gm.engine.dto.OptionBookDto;
import gm.engine.dto.OrderPlacementResultDto;
import gm.engine.exception.GmOperationException;
import gm.engine.model.OrderSide;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Replays the lecturer's reference simulation (targil01/ex2/clob_simulation.html) step by step
 * against the engine: multi-level resale walk, partial fill, peer-to-peer mint, price rejection.
 */
class OrderBookEngineTest {

    private static final String XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Guess-Market xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="GM-EX3-Schema.xsd">
              <GM-events>
                <GM-event name="Rain">
                  <description>Will it rain?</description>
                  <commission type="on-purchase">1</commission>
                  <GM-options><GM-option>YES</GM-option><GM-option>NO</GM-option></GM-options>
                  <GM-method><GM-order-book allow-mint="true" initial="100" d="1"/></GM-method>
                </GM-event>
              </GM-events>
            </Guess-Market>
            """;

    private GmEngineImpl newEngineWithOpenEvent() {
        GmEngineImpl engine = new GmEngineImpl();
        engine.login("Zoe");
        engine.login("Alice");
        engine.login("Bob");
        engine.login("Carol");
        engine.depositFunds("Zoe", 500);
        engine.depositFunds("Alice", 200);
        engine.depositFunds("Bob", 200);
        engine.depositFunds("Carol", 200);
        engine.uploadEventsFile("Zoe", "rain.xml", XML.getBytes(StandardCharsets.UTF_8));
        engine.openEvent("Zoe", 1);
        return engine;
    }

    @Test
    void marketMakerOpenMintsInitialPairs() {
        GmEngineImpl engine = newEngineWithOpenEvent();
        assertEquals(400.0, engine.getUserDetail("Zoe").getBalance(), 0.001);
    }

    @Test
    void resaleWalksMultiplePriceLevels() {
        GmEngineImpl engine = newEngineWithOpenEvent();
        engine.placeOrder("Bob", 1, 1, OrderSide.BUY, 20, 0.50);
        engine.placeOrder("Carol", 1, 1, OrderSide.BUY, 15, 0.48);
        engine.placeOrder("Zoe", 1, 1, OrderSide.SELL, 25, 0.58);
        engine.placeOrder("Zoe", 1, 1, OrderSide.SELL, 15, 0.65);

        OrderPlacementResultDto r = engine.placeOrder("Alice", 1, 1, OrderSide.BUY, 25, 0.58);
        assertEquals(25.0, r.getQuantityFilled(), 0.001);
        assertEquals(0.0, r.getQuantityResting(), 0.001);

        OptionBookDto yesBook = r.getUpdatedEvent().getOptionBooks().get(0);
        assertEquals(0.58, yesBook.getStats().getLast(), 0.001);
        assertEquals(0.65, yesBook.getStats().getAsk(), 0.001);

        r = engine.placeOrder("Zoe", 1, 1, OrderSide.SELL, 30, 0.45);
        assertEquals(30.0, r.getQuantityFilled(), 0.001);
        assertEquals(1, r.getUpdatedEvent().getOptionBooks().get(0).getBids().size());
    }

    @Test
    void peerToPeerMintMatchesReferenceSimulation() {
        GmEngineImpl engine = newEngineWithOpenEvent();
        engine.placeOrder("Bob", 1, 1, OrderSide.BUY, 20, 0.50);
        engine.placeOrder("Carol", 1, 1, OrderSide.BUY, 15, 0.48);
        engine.placeOrder("Zoe", 1, 1, OrderSide.SELL, 25, 0.58);
        engine.placeOrder("Zoe", 1, 1, OrderSide.SELL, 15, 0.65);
        engine.placeOrder("Alice", 1, 1, OrderSide.BUY, 25, 0.58);
        engine.placeOrder("Zoe", 1, 1, OrderSide.SELL, 30, 0.45);

        engine.placeOrder("Carol", 1, 2, OrderSide.BUY, 35, 0.42);
        OrderPlacementResultDto r = engine.placeOrder("Alice", 1, 1, OrderSide.BUY, 40, 0.62);

        assertEquals(35.0, r.getQuantityFilled(), 0.001);
        assertEquals(5.0, r.getQuantityResting(), 0.001);
    }

    @Test
    void rejectsPriceAboveDMinusOneCent() {
        GmEngineImpl engine = newEngineWithOpenEvent();
        assertThrows(GmOperationException.class, () -> engine.placeOrder("Bob", 1, 1, OrderSide.BUY, 10, 1.05));
    }
}
