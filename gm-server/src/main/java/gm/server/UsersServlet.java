package gm.server;

import gm.engine.dto.UserDetailDto;
import gm.engine.exception.GmException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles everything under {@code /api/users}:
 * <pre>
 * GET  /api/users                          -&gt; list of all users
 * GET  /api/users/{username}                -&gt; one user's detail
 * GET  /api/users/{username}/balance-history -&gt; that user's balance-over-time chart data
 * POST /api/users/{username}/deposit         -&gt; { amount } deposits funds, returns updated detail
 * </pre>
 */
@WebServlet("/api/users/*")
public class UsersServlet extends ApiServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                writeJson(response, EngineHolder.ENGINE.getUsers());
                return;
            }
            String[] segments = splitPath(pathInfo);
            String username = segments[0];
            if (segments.length == 1) {
                UserDetailDto detail = EngineHolder.ENGINE.getUserDetail(username);
                writeJson(response, detail);
            } else if (segments.length == 2 && segments[1].equals("balance-history")) {
                writeJson(response, EngineHolder.ENGINE.getBalanceHistory(username));
            } else {
                writeNotFound(response, "No such users endpoint.");
            }
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
            if (segments.length == 2 && segments[1].equals("deposit")) {
                String username = segments[0];
                DepositRequest body = readBody(request, DepositRequest.class);
                UserDetailDto detail = EngineHolder.ENGINE.depositFunds(username, body.amount);
                writeJson(response, detail);
            } else {
                writeNotFound(response, "No such users endpoint.");
            }
        } catch (GmException e) {
            handleEngineException(response, e);
        } catch (Exception e) {
            handleUnexpectedException(response, e);
        }
    }

    private String[] splitPath(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) {
            return new String[0];
        }
        String trimmed = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        String[] raw = trimmed.split("/");
        String[] decoded = new String[raw.length];
        for (int i = 0; i < raw.length; i++) {
            decoded[i] = URLDecoder.decode(raw[i], StandardCharsets.UTF_8);
        }
        return decoded;
    }

    private static final class DepositRequest {
        double amount;
    }
}
