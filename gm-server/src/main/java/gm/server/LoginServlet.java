package gm.server;

import gm.engine.dto.UserSummaryDto;
import gm.engine.exception.GmException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/login")
public class LoginServlet extends ApiServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            LoginRequest body = readBody(request, LoginRequest.class);
            UserSummaryDto user = EngineHolder.ENGINE.login(body.username);
            writeJson(response, user);
        } catch (GmException e) {
            handleEngineException(response, e);
        } catch (Exception e) {
            handleUnexpectedException(response, e);
        }
    }

    private static final class LoginRequest {
        String username;
    }
}
