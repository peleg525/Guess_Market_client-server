package gm.server;

import com.google.gson.Gson;
import gm.engine.exception.GmException;
import gm.engine.exception.GmFileException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.Reader;

/**
 * Shared JSON request/response plumbing for every endpoint servlet. Every {@link GmException} the
 * engine throws (bad file, unknown user/event, wrong actor, insufficient funds, ...) is reported
 * back as HTTP 400 with a JSON body carrying the message - the engine only distinguishes "file
 * problem" from "operation problem", not finer-grained categories, so a single status code for
 * both keeps this mapping honest rather than guessing at 403/404/409 from message text.
 */
public abstract class ApiServlet extends HttpServlet {

    protected final Gson gson = GsonFactory.create();

    protected <T> T readBody(HttpServletRequest request, Class<T> type) throws IOException {
        try (Reader reader = request.getReader()) {
            T value = gson.fromJson(reader, type);
            if (value == null) {
                throw new gm.engine.exception.GmOperationException("Request body must not be empty.");
            }
            return value;
        }
    }

    protected void writeJson(HttpServletResponse response, Object value) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(gson.toJson(value));
    }

    /**
     * Same as {@link #writeJson(HttpServletResponse, Object)}, but serializes against an explicit
     * declared type rather than {@code value.getClass()}. Required whenever the value's static
     * type is the abstract {@link gm.engine.dto.EventDetailDto} - Gson's no-type {@code toJson}
     * picks an adapter by the object's *runtime* class, which bypasses the polymorphic "type"
     * discriminator {@link GsonFactory} registers for the abstract type and would silently produce
     * JSON the client cannot tell apart again.
     */
    protected void writeJson(HttpServletResponse response, Object value, java.lang.reflect.Type type) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(gson.toJson(value, type));
    }

    protected void writeNoContent(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    protected void handleEngineException(HttpServletResponse response, GmException e) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        ErrorBody body = (e instanceof GmFileException fileEx)
                ? new ErrorBody(fileEx.getMessage(), fileEx.getProblems())
                : new ErrorBody(e.getMessage(), null);
        response.getWriter().write(gson.toJson(body));
    }

    protected void handleUnexpectedException(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(gson.toJson(new ErrorBody("Unexpected server error: " + e, null)));
    }

    protected void writeNotFound(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(gson.toJson(new ErrorBody(message, null)));
    }

    private static final class ErrorBody {
        final String message;
        final java.util.List<String> problems;

        ErrorBody(String message, java.util.List<String> problems) {
            this.message = message;
            this.problems = problems;
        }
    }
}
