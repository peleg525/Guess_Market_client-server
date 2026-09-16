package gm.server;

import gm.engine.dto.CloseEventResultDto;
import gm.engine.dto.CreateEventSpecDto;
import gm.engine.dto.EventDetailDto;
import gm.engine.dto.EventFilter;
import gm.engine.dto.OrderPlacementResultDto;
import gm.engine.dto.PurchaseResultDto;
import gm.engine.dto.TradeMethod;
import gm.engine.exception.GmException;
import gm.engine.exception.GmOperationException;
import gm.engine.model.EventStatus;
import gm.engine.model.OrderSide;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Handles everything under {@code /api/events}:
 * <pre>
 * GET  /api/events?method=&amp;status=&amp;commissionType=  -&gt; filtered list (each param repeatable; absent = no filter)
 * GET  /api/events/{id}                              -&gt; event detail
 * GET  /api/events/{id}/price-history?option=N        -&gt; chart data for one option
 * POST /api/events/{id}/open      { username }
 * POST /api/events/{id}/close     { username, winningOption }
 * POST /api/events/{id}/buy-lmsr  { username, option, quantity }
 * POST /api/events/{id}/order     { username, option, side, quantity, price }
 * POST /api/events/create         { username, spec... }  (bonus, carried over from Exercise 2)
 * </pre>
 */
@WebServlet("/api/events/*")
public class EventsServlet extends ApiServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                writeJson(response, EngineHolder.ENGINE.getEvents(parseFilter(request)));
                return;
            }
            String[] segments = splitPath(pathInfo);
            int eventId = Integer.parseInt(segments[0]);
            if (segments.length == 1) {
                writeJson(response, EngineHolder.ENGINE.getEventDetail(eventId), EventDetailDto.class);
            } else if (segments.length == 2 && segments[1].equals("price-history")) {
                int option = Integer.parseInt(request.getParameter("option"));
                writeJson(response, EngineHolder.ENGINE.getPriceHistory(eventId, option));
            } else {
                writeNotFound(response, "No such events endpoint.");
            }
        } catch (NumberFormatException e) {
            handleEngineException(response, new GmOperationException("Invalid event id or option number in the request."));
        } catch (GmException e) {
            handleEngineException(response, e);
        } catch (Exception e) {
            handleUnexpectedException(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String[] segments = splitPath(request.getPathInfo());
            if (segments.length == 1 && segments[0].equals("create")) {
                writeJson(response, handleCreate(request), EventDetailDto.class);
                return;
            }
            int eventId = Integer.parseInt(segments[0]);
            String action = segments.length > 1 ? segments[1] : "";
            switch (action) {
                case "open" -> writeJson(response, handleOpen(request, eventId), EventDetailDto.class);
                case "close" -> writeJson(response, handleClose(request, eventId));
                case "buy-lmsr" -> writeJson(response, handleBuyLmsr(request, eventId));
                case "order" -> writeJson(response, handleOrder(request, eventId));
                default -> writeNotFound(response, "No such events endpoint.");
            }
        } catch (NumberFormatException e) {
            handleEngineException(response, new GmOperationException("Invalid event id in the request."));
        } catch (GmException e) {
            handleEngineException(response, e);
        } catch (Exception e) {
            handleUnexpectedException(response, e);
        }
    }

    private EventDetailDto handleOpen(HttpServletRequest request, int eventId) throws IOException {
        UsernameRequest body = readBody(request, UsernameRequest.class);
        return EngineHolder.ENGINE.openEvent(body.username, eventId);
    }

    private CloseEventResultDto handleClose(HttpServletRequest request, int eventId) throws IOException {
        CloseRequest body = readBody(request, CloseRequest.class);
        return EngineHolder.ENGINE.closeEvent(body.username, eventId, body.winningOption);
    }

    private PurchaseResultDto handleBuyLmsr(HttpServletRequest request, int eventId) throws IOException {
        BuyLmsrRequest body = readBody(request, BuyLmsrRequest.class);
        return EngineHolder.ENGINE.buyLmsrShares(body.username, eventId, body.option, body.quantity);
    }

    private OrderPlacementResultDto handleOrder(HttpServletRequest request, int eventId) throws IOException {
        OrderRequest body = readBody(request, OrderRequest.class);
        OrderSide side = "SELL".equalsIgnoreCase(body.side) ? OrderSide.SELL : OrderSide.BUY;
        return EngineHolder.ENGINE.placeOrder(body.username, eventId, body.option, side, body.quantity, body.price);
    }

    private EventDetailDto handleCreate(HttpServletRequest request) throws IOException {
        CreateRequest body = readBody(request, CreateRequest.class);
        CreateEventSpecDto spec = new CreateEventSpecDto(body.name, body.description, body.commissionPercent,
                body.commissionType, body.option1, body.option2, body.method, body.lmsrB, body.orderBookD,
                body.orderBookInitial, body.orderBookAllowMint);
        return EngineHolder.ENGINE.createEvent(body.username, spec);
    }

    private EventFilter parseFilter(HttpServletRequest request) {
        Set<TradeMethod> methods = new LinkedHashSet<>();
        for (String value : orEmpty(request.getParameterValues("method"))) {
            methods.add(TradeMethod.valueOf(value));
        }
        Set<EventStatus> statuses = new LinkedHashSet<>();
        for (String value : orEmpty(request.getParameterValues("status"))) {
            statuses.add(EventStatus.valueOf(value));
        }
        Set<String> commissionTypes = new LinkedHashSet<>();
        for (String value : orEmpty(request.getParameterValues("commissionType"))) {
            commissionTypes.add(value);
        }
        return new EventFilter(methods, statuses, commissionTypes);
    }

    private String[] orEmpty(String[] values) {
        return values == null ? new String[0] : values;
    }

    private String[] splitPath(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) {
            return new String[0];
        }
        String trimmed = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        return trimmed.split("/");
    }

    private static final class UsernameRequest {
        String username;
    }

    private static final class CloseRequest {
        String username;
        int winningOption;
    }

    private static final class BuyLmsrRequest {
        String username;
        int option;
        double quantity;
    }

    private static final class OrderRequest {
        String username;
        int option;
        String side;
        double quantity;
        double price;
    }

    private static final class CreateRequest {
        String username;
        String name;
        String description;
        int commissionPercent;
        String commissionType;
        String option1;
        String option2;
        TradeMethod method;
        Integer lmsrB;
        Integer orderBookD;
        Integer orderBookInitial;
        Boolean orderBookAllowMint;
    }
}
