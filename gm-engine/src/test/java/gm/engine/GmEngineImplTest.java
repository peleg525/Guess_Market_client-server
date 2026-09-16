package gm.engine;

import gm.engine.dto.ChatMessageDto;
import gm.engine.dto.UserDetailDto;
import gm.engine.dto.UserSummaryDto;
import gm.engine.exception.GmOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercise 3-specific behaviors that have no XML/trading equivalent to reuse tests from: login, deposits, chat. */
class GmEngineImplTest {

    @Test
    void newUserStartsWithZeroBalance() {
        GmEngineImpl engine = new GmEngineImpl();
        UserSummaryDto user = engine.login("Alice");
        assertEquals(0.0, user.getBalance(), 0.001);
        assertFalse(user.isBlocked());
    }

    @Test
    void loginRejectsUsernameAlreadyConnected() {
        GmEngineImpl engine = new GmEngineImpl();
        engine.login("Alice");
        GmOperationException ex = assertThrows(GmOperationException.class, () -> engine.login("Alice"));
        assertTrue(ex.getMessage().contains("already taken"), ex.getMessage());
    }

    @Test
    void loginIsCaseInsensitiveForAvailability() {
        GmEngineImpl engine = new GmEngineImpl();
        engine.login("Alice");
        assertThrows(GmOperationException.class, () -> engine.login("ALICE"));
    }

    @Test
    void logoutFreesTheNameAndKeepsTheAccount() {
        GmEngineImpl engine = new GmEngineImpl();
        engine.login("Alice");
        engine.depositFunds("Alice", 50);
        engine.logout("Alice");

        UserSummaryDto reconnected = engine.login("Alice");
        assertEquals(50.0, reconnected.getBalance(), 0.001);
    }

    @Test
    void depositRequiresPositiveAmount() {
        GmEngineImpl engine = new GmEngineImpl();
        engine.login("Alice");
        assertThrows(GmOperationException.class, () -> engine.depositFunds("Alice", 0));
        assertThrows(GmOperationException.class, () -> engine.depositFunds("Alice", -5));
    }

    @Test
    void depositIncreasesBalanceAndAppearsInHistory() {
        GmEngineImpl engine = new GmEngineImpl();
        engine.login("Alice");
        UserDetailDto detail = engine.depositFunds("Alice", 250);
        assertEquals(250.0, detail.getBalance(), 0.001);
        assertEquals(1, engine.getBalanceHistory("Alice").size());
    }

    @Test
    void chatMessagesAreReturnedInOrderAndOnlyAfterTheGivenSequence() {
        GmEngineImpl engine = new GmEngineImpl();
        engine.login("Alice");
        engine.login("Bob");
        engine.postChatMessage("Alice", "hi");
        engine.postChatMessage("Bob", "hello");

        List<ChatMessageDto> all = engine.getChatMessages(0);
        assertEquals(2, all.size());
        assertEquals("Alice", all.get(0).getUsername());
        assertEquals("Bob", all.get(1).getUsername());

        List<ChatMessageDto> onlyLatest = engine.getChatMessages(all.get(0).getSequence());
        assertEquals(1, onlyLatest.size());
        assertEquals("Bob", onlyLatest.get(0).getUsername());
    }

    @Test
    void chatRejectsBlankMessages() {
        GmEngineImpl engine = new GmEngineImpl();
        engine.login("Alice");
        assertThrows(GmOperationException.class, () -> engine.postChatMessage("Alice", "   "));
    }
}
