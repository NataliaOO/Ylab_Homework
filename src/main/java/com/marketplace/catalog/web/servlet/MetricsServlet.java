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

    private static final String ATTR_CURRENT_USER = "currentUser";

    private static final String MSG_NOT_AUTHORIZED   = "Not authorized";
    private static final String MSG_LOGIN_REQUIRED   = "Login required";
    private static final String MSG_ADMIN_REQUIRED   = "Admin role required";

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
        prepareJson(resp);

        User user = getCurrentUser(req);
        if (user == null) {
            sendUnauthorized(resp, MSG_NOT_AUTHORIZED, List.of(MSG_LOGIN_REQUIRED));
            return;
        }
        if (user.getRole() != Role.ADMIN) {
            sendForbidden(resp, MSG_ADMIN_REQUIRED);
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
        return (User) session.getAttribute(ATTR_CURRENT_USER);
    }

    private void sendUnauthorized(HttpServletResponse resp, String message, List<String> details) throws IOException {
        sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, message, details);
    }

    private void sendForbidden(HttpServletResponse resp, String message) throws IOException {
        sendError(resp, HttpServletResponse.SC_FORBIDDEN, message, null);
    }

    private void sendError(HttpServletResponse resp, int status, String message, List<String> details) throws IOException {
        resp.setStatus(status);
        objectMapper.writeValue(resp.getWriter(), new ErrorResponse(message, details));
    }

    private void prepareJson(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
    }
}
