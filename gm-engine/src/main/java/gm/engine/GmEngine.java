package gm.engine;

import gm.engine.dto.BalancePointDto;
import gm.engine.dto.ChatMessageDto;
import gm.engine.dto.CloseEventResultDto;
import gm.engine.dto.CreateEventSpecDto;
import gm.engine.dto.EventDetailDto;
import gm.engine.dto.EventFilter;
import gm.engine.dto.EventSummaryDto;
import gm.engine.dto.LoadResultDto;
import gm.engine.dto.OrderPlacementResultDto;
import gm.engine.dto.PricePointDto;
import gm.engine.dto.PurchaseResultDto;
import gm.engine.dto.UserDetailDto;
import gm.engine.dto.UserSummaryDto;
import gm.engine.exception.GmFileException;
import gm.engine.exception.GmOperationException;
import gm.engine.model.OrderSide;

import java.util.List;

/**
 * Everything a caller (the HTTP servlet layer on the server, or the HTTP-backed adapter on the
 * client) is allowed to know about the Guess Market engine. Every user-facing option/event number
 * here is 1-based, matching the rest of the assignment's UI conventions; the engine translates
 * to/from 0-based indices internally.
 * <p>
 * Unlike Exercise 1/2, there is no single "acting user" combo box standing in for a login - every
 * mutating method takes the actor's username explicitly, and {@link #login(String)} is how a
 * username is first registered with the running server. There is also no XML-driven user list any
 * more: users are created purely through {@link #login(String)}, and events accumulate across
 * multiple {@link #uploadEventsFile(String, String, byte[])} calls instead of replacing each other.
 */
public interface GmEngine {

    /**
     * Registers a brand-new username with the system, or - if a client reconnects with a name it
     * already owns from an earlier call in the same server lifetime - simply returns that user
     * again. Fails only when the name is currently in use by a different, still-connected client.
     *
     * @throws GmOperationException if the username is blank or already taken
     */
    UserSummaryDto login(String username);

    /** Marks a username as free again so someone else may log in with it. Never fails. */
    void logout(String username);

    List<UserSummaryDto> getUsers();

    /** @throws GmOperationException if no such user exists */
    UserDetailDto getUserDetail(String username);

    /** Adds funds to a user's account. @throws GmOperationException if the user is unknown, blocked, or the amount isn't positive */
    UserDetailDto depositFunds(String username, double amount);

    /** Bonus: chart data - a user's account balance over time. */
    List<BalancePointDto> getBalanceHistory(String username);

    /**
     * Validates and adds every event in the given XML file's content to the system. Files
     * accumulate - a successful upload never removes events added by earlier uploads. The
     * uploading user becomes the market maker of every event the file contains.
     *
     * @throws GmFileException if the file name, its XML, or its content is invalid
     */
    LoadResultDto uploadEventsFile(String uploaderUsername, String fileName, byte[] xmlContent);

    List<EventSummaryDto> getEvents(EventFilter filter);

    /** @throws GmOperationException if no such event exists */
    EventDetailDto getEventDetail(int eventId);

    /** @throws GmOperationException if the user isn't this event's market maker, or it's already open */
    EventDetailDto openEvent(String marketMakerUsername, int eventId);

    /** @throws GmOperationException if the user isn't this event's market maker, or it isn't active */
    CloseEventResultDto closeEvent(String marketMakerUsername, int eventId, int winningOptionNumber);

    /** LMSR purchase. @throws GmOperationException if the event isn't LMSR, isn't active, or funds are insufficient */
    PurchaseResultDto buyLmsrShares(String username, int eventId, int optionNumber, double quantity);

    /** Order-book order. @throws GmOperationException if the event isn't order-book, isn't active, or the order is invalid */
    OrderPlacementResultDto placeOrder(String username, int eventId, int optionNumber, OrderSide side,
                                        double quantity, double price);

    /** Bonus: creates a brand-new event with the acting user as its market maker. */
    EventDetailDto createEvent(String marketMakerUsername, CreateEventSpecDto spec);

    /** Bonus: chart data - executed trade prices for one option, in chronological order. */
    List<PricePointDto> getPriceHistory(int eventId, int optionNumber);

    /** Bonus: posts a message to the system-wide chat room. @throws GmOperationException if the user is unknown or the text is blank */
    void postChatMessage(String username, String text);

    /** Bonus: every chat message with a sequence number greater than {@code sinceSequence}, oldest first. */
    List<ChatMessageDto> getChatMessages(long sinceSequence);
}
