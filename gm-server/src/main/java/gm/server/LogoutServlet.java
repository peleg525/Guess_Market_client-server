package gm.server;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/logout")
public class LogoutServlet extends ApiServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            LogoutRequest body = readBody(request, LogoutRequest.class);
            EngineHolder.ENGINE.logout(body.username);
            writeNoContent(response);
        } catch (Exception e) {
            handleUnexpectedException(response, e);
        }
    }

    private static final class LogoutRequest {
        String username;
    }
}
