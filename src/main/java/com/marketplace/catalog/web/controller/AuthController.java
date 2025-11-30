package com.marketplace.catalog.web.controller;

import com.marketplace.catalog.model.User;
import com.marketplace.catalog.service.AuthService;
import com.marketplace.catalog.web.dto.AuthRequest;
import com.marketplace.catalog.web.dto.ErrorResponse;
import com.marketplace.catalog.web.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String ATTR_CURRENT_USER = "currentUser";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest,
                                   HttpServletRequest request) {
        var userOpt = authService.login(authRequest.login(), authRequest.password());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid credentials"));
        }

        User user = userOpt.get();
        HttpSession session = request.getSession(true);
        session.setAttribute(ATTR_CURRENT_USER, user);

        UserDto dto = new UserDto(
                user.getId(),
                user.getLogin(),
                user.getRole() != null ? user.getRole().name() : null
        );
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute(ATTR_CURRENT_USER);
            if (user != null) {
                authService.logout(user.getLogin());
            }
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Not authorized"));
        }

        UserDto dto = new UserDto(
                user.getId(),
                user.getLogin(),
                user.getRole() != null ? user.getRole().name() : null
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
