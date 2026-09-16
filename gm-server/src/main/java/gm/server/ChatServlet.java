package gm.server;

import gm.engine.exception.GmException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Bonus: a system-wide chat room, polled the same way as everything else in this exercise.
 * <pre>
 * GET  /api/chat?since=N   -&gt; every message with sequence &gt; N, oldest first
 * POST /api/chat           -&gt; { username, text }
 * </pre>
 */
@WebServlet("/api/chat")
public class ChatServlet extends ApiServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String sinceParam = request.getParameter("since");
            long since = (sinceParam == null || sinceParam.isBlank()) ? 0 : Long.parseLong(sinceParam);
            writeJson(response, EngineHolder.ENGINE.getChatMessages(since));
        } catch (NumberFormatException e) {
            handleEngineException(response, new gm.engine.exception.GmOperationException("Invalid \"since\" parameter."));
        } catch (Exception e) {
            handleUnexpectedException(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ChatRequest body = readBody(request, ChatRequest.class);
            EngineHolder.ENGINE.postChatMessage(body.username, body.text);
            writeNoContent(response);
        } catch (GmException e) {
            handleEngineException(response, e);
        } catch (Exception e) {
            handleUnexpectedException(response, e);
        }
    }

    private static final class ChatRequest {
        String username;
        String text;
    }
}
