package com.marketplace.catalog.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.catalog.model.Role;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.service.AuthService;
import com.marketplace.catalog.web.json.ObjectMapperFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.marketplace.catalog.TestUtils.toServletInputStream;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServletTest {

    private AuthService authService;
    private AuthServlet servlet;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        objectMapper = ObjectMapperFactory.get();
        servlet = new AuthServlet(authService, objectMapper);
    }

    @Test
    void login_ok_shouldSetSessionAndReturn200() throws Exception {
        HttpServletRequest req  = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getPathInfo()).thenReturn("/login");
        when(req.getSession(true)).thenReturn(session);

        String json = "{\"login\":\"admin\",\"password\":\"secret\"}";
        when(req.getInputStream()).thenReturn(toServletInputStream(json));

        when(authService.login("admin", "secret")).thenReturn(true);

        User current = new User(1L, "admin", "secret", Role.ADMIN);
        when(authService.getCurrentUser()).thenReturn(current);

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doPost(req, resp);

        verify(authService).login("admin", "secret");
        verify(session).setAttribute(eq("currentUser"), same(current));
        verify(resp).setStatus(HttpServletResponse.SC_OK);

        String body = sw.toString();
        assertTrue(body.contains("\"login\":\"admin\""));
    }

    @Test
    void login_invalidCredentials_shouldReturn401() throws Exception {
        HttpServletRequest req  = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getPathInfo()).thenReturn("/login");

        String json = "{\"login\":\"admin\",\"password\":\"wrong\"}";
        when(req.getInputStream()).thenReturn(toServletInputStream(json));

        when(authService.login("admin", "wrong")).thenReturn(false);

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(authService, never()).getCurrentUser();
        String body = sw.toString();
        assertTrue(body.contains("Invalid login or password"));
    }

    @Test
    void me_notLoggedIn_shouldReturn401() throws Exception {
        HttpServletRequest req  = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getPathInfo()).thenReturn("/me");
        when(req.getSession(false)).thenReturn(null);

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doGet(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String body = sw.toString();
        assertTrue(body.contains("Not logged in"));
    }
}
