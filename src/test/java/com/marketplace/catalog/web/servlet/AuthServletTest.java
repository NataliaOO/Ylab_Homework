package com.marketplace.catalog.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.catalog.model.Role;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.service.AuthService;
import com.marketplace.catalog.web.dto.ErrorResponse;
import com.marketplace.catalog.web.dto.UserDto;
import com.marketplace.catalog.web.json.ObjectMapperFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.marketplace.catalog.web.servlet.TestUtils.toServletInputStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuthServletTest {

    private AuthService authService;
    private AuthServlet servlet;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.authService = mock(AuthService.class);
        this.objectMapper = ObjectMapperFactory.get();
        this.servlet = new AuthServlet(authService, objectMapper);
    }

    @Test
    void givenValidCredentials_whenLogin_thenSessionIsCreatedAndUserReturned() throws Exception {
        // given
        HttpServletRequest req  = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getPathInfo()).thenReturn("/login");
        when(req.getSession(true)).thenReturn(session);

        String json = """
                {"login":"admin","password":"secret"}
                """;
        when(req.getInputStream()).thenReturn(toServletInputStream(json));

        when(authService.login("admin", "secret")).thenReturn(true);

        User current = new User(1L, "admin", "secret", Role.ADMIN);
        when(authService.getCurrentUser()).thenReturn(current);

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        // when
        servlet.doPost(req, resp);

        // then
        verify(authService).login("admin", "secret");
        verify(session).setAttribute(eq("currentUser"), same(current));
        verify(resp).setStatus(HttpServletResponse.SC_OK);

        UserDto body = objectMapper.readValue(sw.toString(), UserDto.class);
        assertEquals(1L, body.id());
        assertEquals("admin", body.login());
        assertEquals("ADMIN", body.role());
    }

    @Test
    void givenInvalidCredentials_whenLogin_then401AndErrorReturned() throws Exception {
        // given
        HttpServletRequest req  = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getPathInfo()).thenReturn("/login");

        String json = """
                {"login":"admin","password":"wrong"}
                """;
        when(req.getInputStream()).thenReturn(toServletInputStream(json));

        when(authService.login("admin", "wrong")).thenReturn(false);

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        // when
        servlet.doPost(req, resp);

        // then
        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(authService, never()).getCurrentUser();

        ErrorResponse error = objectMapper.readValue(sw.toString(), ErrorResponse.class);
        assertEquals("Invalid login or password", error.message());
    }


    @Test
    void givenNoSession_whenMe_then401AndNotLoggedInError() throws Exception {
        // given
        HttpServletRequest req  = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getPathInfo()).thenReturn("/me");
        when(req.getSession(false)).thenReturn(null);

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        // when
        servlet.doGet(req, resp);

        // then
        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse error = objectMapper.readValue(sw.toString(), ErrorResponse.class);
        assertEquals("Not logged in", error.message());
    }

    @Test
    void givenLoggedInUser_whenMe_then200AndUserReturned() throws Exception {
        // given
        HttpServletRequest req  = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getPathInfo()).thenReturn("/me");
        when(req.getSession(false)).thenReturn(session);

        User current = new User(1L, "admin", "secret", Role.ADMIN);
        when(session.getAttribute("currentUser")).thenReturn(current);

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        // when
        servlet.doGet(req, resp);

        // then
        verify(resp).setStatus(HttpServletResponse.SC_OK);

        UserDto dto = objectMapper.readValue(sw.toString(), UserDto.class);
        assertEquals(1L, dto.id());
        assertEquals("admin", dto.login());
        assertEquals("ADMIN", dto.role());
    }

    @Test
    void givenSession_whenLogout_thenCurrentUserRemovedAnd204() throws Exception {
        // given
        HttpServletRequest req  = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getPathInfo()).thenReturn("/logout");
        when(req.getSession(false)).thenReturn(session);

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        // when
        servlet.doPost(req, resp);

        // then
        verify(session).removeAttribute("currentUser");
        verify(authService).logout();
        verify(resp).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
