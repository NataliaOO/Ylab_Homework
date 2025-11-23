package com.marketplace.catalog.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.catalog.config.AppContext;
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

        String path = req.getPathInfo(); // /login или /logout
        if (path == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Unknown auth endpoint", null));
            return;
        }

        switch (path) {
            case "/login" -> handleLogin(req, resp);
            case "/logout" -> handleLogout(req, resp);
            default -> {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(),
                        new ErrorResponse("Unknown auth endpoint", null));
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareJson(resp);

        String path = req.getPathInfo(); // /me
        if ("/me".equals(path)) {
            handleMe(req, resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Unknown auth endpoint", null));
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthRequest authRequest = objectMapper.readValue(req.getInputStream(), AuthRequest.class);

        boolean ok = authService.login(authRequest.getLogin(), authRequest.getPassword());
        if (!ok) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Invalid login or password", null));
            return;
        }

        var user = authService.getCurrentUser();

        HttpSession session = req.getSession(true);
        session.setAttribute("currentUser", user);

        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(),
                new UserDto(user.getId(), user.getLogin(),
                        user.getRole() != null ? user.getRole().name() : null));
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.removeAttribute("currentUser");
        }
        authService.logout();

        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private void handleMe(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Not logged in", null));
            return;
        }

        var user = (com.marketplace.catalog.model.User) session.getAttribute("currentUser");
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Not logged in", null));
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(),
                new UserDto(user.getId(), user.getLogin(),
                        user.getRole() != null ? user.getRole().name() : null));
    }

    private void prepareJson(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
    }
}
