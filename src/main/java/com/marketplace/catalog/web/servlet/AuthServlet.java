package com.marketplace.catalog.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.catalog.config.AppContext;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.service.AuthService;
import com.marketplace.catalog.web.dto.AuthRequest;
import com.marketplace.catalog.web.dto.ErrorResponse;
import com.marketplace.catalog.web.dto.UserDto;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "AuthServlet", urlPatterns = "/api/auth/*")
public class AuthServlet extends HttpServlet {

    private static final String ATTR_CURRENT_USER = "currentUser";

    private static final String MSG_UNKNOWN_ENDPOINT    = "Unknown auth endpoint";
    private static final String MSG_INVALID_CREDENTIALS = "Invalid login or password";
    private static final String MSG_NOT_LOGGED_IN       = "Not logged in";

    private AuthService authService;
    private ObjectMapper objectMapper;

    public AuthServlet(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    public AuthServlet() {
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        AppContext ctx = (AppContext) config.getServletContext().getAttribute("appContext");
        this.authService  = ctx.getAuthService();
        this.objectMapper = ctx.getObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareJson(resp);

        String path = req.getPathInfo();
        if (path == null) {
            sendUnknownEndpoint(resp);
            return;
        }

        switch (path) {
            case "/login" -> handleLogin(req, resp);
            case "/logout" -> handleLogout(req, resp);
            default -> sendUnknownEndpoint(resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareJson(resp);

        String path = req.getPathInfo(); // /me
        if ("/me".equals(path)) {
            handleMe(req, resp);
        } else {
            sendUnknownEndpoint(resp);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthRequest authRequest = objectMapper.readValue(req.getInputStream(), AuthRequest.class);

        var userOpt = authService.login(authRequest.getLogin(), authRequest.getPassword());
        if (userOpt.isEmpty()) {
            sendInvalidCredentials(resp);
            return;
        }

        User user = userOpt.get();
        HttpSession session = req.getSession(true);
        session.setAttribute(ATTR_CURRENT_USER, user);

        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(),
                new UserDto(user.getId(), user.getLogin(),
                        user.getRole() != null ? user.getRole().name() : null));
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute(ATTR_CURRENT_USER);
            if (user != null) {
                authService.logout(user.getLogin());
            }
            session.invalidate();
        }
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private void handleMe(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getCurrentUserOrSend401(req, resp);
        if (user == null) {
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(),
                new UserDto(
                        user.getId(),
                        user.getLogin(),
                        user.getRole() != null ? user.getRole().name() : null
                )
        );
    }

    private User getCurrentUserOrSend401(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            sendNotLoggedIn(resp);
            return null;
        }

        User user = (User) session.getAttribute(ATTR_CURRENT_USER);
        if (user == null) {
            sendNotLoggedIn(resp);
            return null;
        }
        return user;
    }

    private void sendUnknownEndpoint(HttpServletResponse resp) throws IOException {
        sendError(resp, HttpServletResponse.SC_NOT_FOUND, MSG_UNKNOWN_ENDPOINT);
    }

    private void sendInvalidCredentials(HttpServletResponse resp) throws IOException {
        sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, MSG_INVALID_CREDENTIALS);
    }

    private void sendNotLoggedIn(HttpServletResponse resp) throws IOException {
        sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, MSG_NOT_LOGGED_IN);
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        objectMapper.writeValue(resp.getWriter(), new ErrorResponse(message, null));
    }

    private void prepareJson(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
    }
}
