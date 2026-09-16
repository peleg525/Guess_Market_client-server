package gm.engine;

import gm.engine.dto.CloseEventResultDto;
import gm.engine.dto.LmsrEventDetailDto;
import gm.engine.dto.PurchaseResultDto;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Reproduces Appendix A's worked LMSR example (b=100, buy 100 YES shares, then close). */
class LmsrEngineTest {

    private static final String XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Guess-Market xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="GM-EX3-Schema.xsd">
              <GM-events>
                <GM-event name="Coin flip">
                  <description>Will it land heads?</description>
                  <commission type="on-purchase">0</commission>
                  <GM-options><GM-option>YES</GM-option><GM-option>NO</GM-option></GM-options>
                  <GM-method><GM-LMSR><b>100</b></GM-LMSR></GM-method>
                </GM-event>
              </GM-events>
            </Guess-Market>
            """;

    @Test
    void matchesAppendixAWorkedExample() {
        GmEngineImpl engine = new GmEngineImpl();
        engine.login("Mm");
        engine.login("Buyer");
        engine.depositFunds("Mm", 1000);
        engine.depositFunds("Buyer", 1000);
        engine.uploadEventsFile("Mm", "coin.xml", XML.getBytes(StandardCharsets.UTF_8));
        engine.openEvent("Mm", 1);

        LmsrEventDetailDto afterOpen = (LmsrEventDetailDto) engine.getEventDetail(1);
        assertEquals(69.31, afterOpen.getEventAccountBalance(), 0.01);

        PurchaseResultDto purchase = engine.buyLmsrShares("Buyer", 1, 1, 100);
        assertEquals(62.01, purchase.getSharesCost(), 0.01);

        LmsrEventDetailDto afterBuy = (LmsrEventDetailDto) purchase.getUpdatedEvent();
        assertEquals(0.73, afterBuy.getOptionStatuses().get(0).getCurrentPrice(), 0.01);
        assertEquals(131.32, afterBuy.getEventAccountBalance(), 0.01);

        CloseEventResultDto closeResult = engine.closeEvent("Mm", 1, 1);
        assertEquals(1000 - purchase.getSharesCost() + 100, engine.getUserDetail("Buyer").getBalance(), 0.001);
        assertEquals(false, closeResult.isMarketMakerBlocked());
    }
}
