package com.marketplace.catalog.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.catalog.config.AppContext;
import com.marketplace.catalog.model.Role;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.service.Metrics;
import com.marketplace.catalog.web.dto.ErrorResponse;
import com.marketplace.catalog.web.dto.MetricsDto;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "MetricsServlet", urlPatterns = "/api/metrics")
public class MetricsServlet extends HttpServlet {

    private Metrics metrics;
    private ObjectMapper objectMapper;

    public MetricsServlet() {
    }

    public MetricsServlet(Metrics metrics, ObjectMapper objectMapper) {
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        AppContext ctx = (AppContext) config.getServletContext().getAttribute("appContext");

        this.metrics      = ctx.getMetrics();
        this.objectMapper = ctx.getObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        User user = getCurrentUser(req);
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Not authorized", List.of("Login required")));
            return;
        }
        if (user.getRole() != Role.ADMIN) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(resp.getWriter(),
                    new ErrorResponse("Admin role required", null));
            return;
        }

        MetricsDto dto = new MetricsDto(
                metrics.getCreateCount(),
                metrics.getUpdateCount(),
                metrics.getDeleteCount(),
                metrics.getSearchCount(),
                metrics.getCacheHitCount(),
                metrics.getAverageSearchTimeMillis(),
                metrics.getCacheHitRatio()
        );

        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(), dto);
    }

    private User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("currentUser");
    }
}
