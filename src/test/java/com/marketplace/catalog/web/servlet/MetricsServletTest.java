package com.marketplace.catalog.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.catalog.model.Role;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.service.Metrics;
import com.marketplace.catalog.web.dto.MetricsDto;
import com.marketplace.catalog.web.json.ObjectMapperFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MetricsServletTest {

    private Metrics metrics;
    private MetricsServlet servlet;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.metrics = mock(Metrics.class);
        this.objectMapper = ObjectMapperFactory.get();
        this.servlet = new MetricsServlet(metrics, objectMapper);
    }

    @Test
    void getMetrics_adminUser_returnsMetricsDtoAnd200() throws Exception {
        // given
        HttpServletRequest req  = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser"))
                .thenReturn(new User(1L, "admin", "pwd", Role.ADMIN));

        when(metrics.getCreateCount()).thenReturn(5L);
        when(metrics.getUpdateCount()).thenReturn(2L);
        when(metrics.getDeleteCount()).thenReturn(1L);
        when(metrics.getSearchCount()).thenReturn(10L);
        when(metrics.getCacheHitCount()).thenReturn(7L);
        when(metrics.getAverageSearchTimeMillis()).thenReturn(3.5);
        when(metrics.getCacheHitRatio()).thenReturn(0.7);

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        // when
        servlet.doGet(req, resp);

        // then
        verify(resp).setStatus(HttpServletResponse.SC_OK);

        String body = sw.toString();
        MetricsDto dto = objectMapper.readValue(body, MetricsDto.class);

        assertEquals(5L, dto.createCount());
        assertEquals(2L, dto.updateCount());
        assertEquals(1L, dto.deleteCount());
        assertEquals(10L, dto.searchCount());
        assertEquals(7L, dto.cacheHitCount());
        assertEquals(3.5, dto.averageSearchTimeMillis());
        assertEquals(0.7, dto.cacheHitRatio());
    }

    @Test
    void getMetrics_withoutSession_returns401() throws Exception {
        // given
        HttpServletRequest req  = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenReturn(null);

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        // when
        servlet.doGet(req, resp);

        // then
        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // по желанию можно ещё распарсить ErrorResponse и проверить сообщение
    }

    @Test
    void getMetrics_nonAdmin_returns403() throws Exception {
        // given
        HttpServletRequest req  = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser"))
                .thenReturn(new User(2L, "viewer", "pwd", Role.VIEWER));

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        // when
        servlet.doGet(req, resp);

        // then
        verify(resp).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}
