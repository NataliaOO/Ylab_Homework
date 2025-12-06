package com.marketplace.catalog.web.controller;

import com.marketplace.catalog.model.Role;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.service.Metrics;
import com.marketplace.catalog.web.dto.ErrorResponse;
import com.marketplace.catalog.web.dto.MetricsDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.marketplace.catalog.web.controller.AuthController.ATTR_CURRENT_USER;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final Metrics metrics;

    public MetricsController(Metrics metrics) {
        this.metrics = metrics;
    }

    @GetMapping
    public ResponseEntity<?> getMetrics(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Not authorized"));
        }
        if (user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Admin role required"));
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

        return ResponseEntity.ok(dto);
    }

    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null
                ? (User) session.getAttribute(ATTR_CURRENT_USER)
                : null;
    }
}
