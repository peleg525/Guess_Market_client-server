package gm.ui.net;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import gm.engine.GmEngine;
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

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * A {@link GmEngine} whose every method is really an HTTP call to the Guess Market server. This is
 * the seam that lets every existing Exercise 2 screen/component keep working unmodified: they all
 * talk to {@code AppContext.engine()}, typed as the {@link GmEngine} interface, and have no idea
 * whether the object behind it is the real in-memory engine or (as here) a thin remote proxy.
 */
public class GmHttpEngine implements GmEngine {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
    private final Gson gson = GsonFactory.create();
    private final String apiBaseUrl;

    public GmHttpEngine(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl;
    }

    @Override
    public UserSummaryDto login(String username) {
        return postJson("/login", jsonOf("username", username), UserSummaryDto.class);
    }

    @Override
    public void logout(String username) {
        postJson("/logout", jsonOf("username", username), null);
    }

    @Override
    public List<UserSummaryDto> getUsers() {
        return get("/users", new TypeToken<List<UserSummaryDto>>() { }.getType());
    }

    @Override
    public UserDetailDto getUserDetail(String username) {
        return get("/users/" + encode(username), UserDetailDto.class);
    }

    @Override
    public UserDetailDto depositFunds(String username, double amount) {
        JsonObject body = new JsonObject();
        body.addProperty("amount", amount);
        return postJson("/users/" + encode(username) + "/deposit", body, UserDetailDto.class);
    }

    @Override
    public List<BalancePointDto> getBalanceHistory(String username) {
        return get("/users/" + encode(username) + "/balance-history", new TypeToken<List<BalancePointDto>>() { }.getType());
    }

    @Override
    public LoadResultDto uploadEventsFile(String uploaderUsername, String fileName, byte[] xmlContent) {
        MultipartBodyBuilder multipart = new MultipartBodyBuilder()
                .addField("username", uploaderUsername)
                .addFile("file", fileName, xmlContent);
        HttpRequest request = HttpRequest.newBuilder(uri("/files"))
                .timeout(TIMEOUT)
                .header("Content-Type", multipart.contentType())
                .POST(multipart.build())
                .build();
        return send(request, LoadResultDto.class);
    }

    @Override
    public List<EventSummaryDto> getEvents(EventFilter filter) {
        // EventFilter exposes only matchesX(...) predicates, not its raw criteria sets, so there is
        // no way to turn it back into query params here. Fetching the unfiltered list and applying
        // the same predicates client-side is simple and correct (the server also supports
        // ?method=/&status=/&commissionType= query filtering directly for other kinds of callers).
        List<EventSummaryDto> all = get("/events", new TypeToken<List<EventSummaryDto>>() { }.getType());
        if (filter == null) {
            return all;
        }
        return all.stream()
                .filter(e -> filter.matchesMethod(e.getMethod())
                        && filter.matchesStatus(e.getStatus())
                        && filter.matchesCommissionType(e.getCommissionType()))
                .toList();
    }

    @Override
    public EventDetailDto getEventDetail(int eventId) {
        return get("/events/" + eventId, EventDetailDto.class);
    }

    @Override
    public EventDetailDto openEvent(String marketMakerUsername, int eventId) {
        return postJson("/events/" + eventId + "/open", jsonOf("username", marketMakerUsername), EventDetailDto.class);
    }

    @Override
    public CloseEventResultDto closeEvent(String marketMakerUsername, int eventId, int winningOptionNumber) {
        JsonObject body = new JsonObject();
        body.addProperty("username", marketMakerUsername);
        body.addProperty("winningOption", winningOptionNumber);
        return postJson("/events/" + eventId + "/close", body, CloseEventResultDto.class);
    }

    @Override
    public PurchaseResultDto buyLmsrShares(String username, int eventId, int optionNumber, double quantity) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("option", optionNumber);
        body.addProperty("quantity", quantity);
        return postJson("/events/" + eventId + "/buy-lmsr", body, PurchaseResultDto.class);
    }

    @Override
    public OrderPlacementResultDto placeOrder(String username, int eventId, int optionNumber, OrderSide side,
                                               double quantity, double price) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("option", optionNumber);
        body.addProperty("side", side.name());
        body.addProperty("quantity", quantity);
        body.addProperty("price", price);
        return postJson("/events/" + eventId + "/order", body, OrderPlacementResultDto.class);
    }

    @Override
    public EventDetailDto createEvent(String marketMakerUsername, CreateEventSpecDto spec) {
        JsonObject body = new JsonObject();
        body.addProperty("username", marketMakerUsername);
        body.addProperty("name", spec.getName());
        body.addProperty("description", spec.getDescription());
        body.addProperty("commissionPercent", spec.getCommissionPercent());
        body.addProperty("commissionType", spec.getCommissionType());
        body.addProperty("option1", spec.getOption1());
        body.addProperty("option2", spec.getOption2());
        body.addProperty("method", spec.getMethod().name());
        addIfNotNull(body, "lmsrB", spec.getLmsrB());
        addIfNotNull(body, "orderBookD", spec.getOrderBookD());
        addIfNotNull(body, "orderBookInitial", spec.getOrderBookInitial());
        if (spec.getOrderBookAllowMint() != null) {
            body.addProperty("orderBookAllowMint", spec.getOrderBookAllowMint());
        }
        return postJson("/events/create", body, EventDetailDto.class);
    }

    @Override
    public List<PricePointDto> getPriceHistory(int eventId, int optionNumber) {
        return get("/events/" + eventId + "/price-history?option=" + optionNumber, new TypeToken<List<PricePointDto>>() { }.getType());
    }

    @Override
    public void postChatMessage(String username, String text) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("text", text);
        postJson("/chat", body, null);
    }

    @Override
    public List<ChatMessageDto> getChatMessages(long sinceSequence) {
        return get("/chat?since=" + sinceSequence, new TypeToken<List<ChatMessageDto>>() { }.getType());
    }

    // ---- HTTP plumbing ----

    private <T> T get(String path, Type responseType) {
        HttpRequest request = HttpRequest.newBuilder(uri(path)).timeout(TIMEOUT).GET().build();
        return send(request, responseType);
    }

    private <T> T postJson(String path, JsonObject body, Type responseType) {
        HttpRequest request = HttpRequest.newBuilder(uri(path))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json;charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8))
                .build();
        return send(request, responseType);
    }

    private <T> T send(HttpRequest request, Type responseType) {
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new GmOperationException("Could not reach the server at " + apiBaseUrl + ": " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GmOperationException("The request to the server was interrupted.");
        }

        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            if (responseType == null || response.body() == null || response.body().isBlank()) {
                return null;
            }
            return gson.fromJson(response.body(), responseType);
        }
        throw translateError(status, response.body());
    }

    private RuntimeException translateError(int status, String body) {
        String message = "The server returned an unexpected error (HTTP " + status + ").";
        List<String> problems = null;
        try {
            JsonObject json = gson.fromJson(body, JsonObject.class);
            if (json != null && json.has("message") && !json.get("message").isJsonNull()) {
                message = json.get("message").getAsString();
            }
            if (json != null && json.has("problems") && json.get("problems").isJsonArray()) {
                problems = gson.fromJson(json.get("problems"), new TypeToken<List<String>>() { }.getType());
            }
        } catch (RuntimeException ignored) {
            // body wasn't valid JSON - fall back to the generic message above
        }
        return (problems != null && !problems.isEmpty()) ? new GmFileException(problems) : new GmOperationException(message);
    }

    private JsonObject jsonOf(String key, String value) {
        JsonObject object = new JsonObject();
        object.addProperty(key, value);
        return object;
    }

    private void addIfNotNull(JsonObject object, String key, Integer value) {
        if (value != null) {
            object.addProperty(key, value);
        }
    }

    private URI uri(String path) {
        return URI.create(apiBaseUrl + path);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
