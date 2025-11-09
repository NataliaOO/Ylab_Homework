package com.marketplace.catalog.service;

import com.marketplace.catalog.model.Role;
import com.marketplace.catalog.repository.AuditRepository;
import com.marketplace.catalog.repository.InMemoryAuditRepository;
import com.marketplace.catalog.repository.InMemoryUserRepository;
import com.marketplace.catalog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private AuditRepository auditRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        auditRepository = new InMemoryAuditRepository();
        authService = new AuthService(userRepository, auditRepository);
    }

    @Test
    void login_withValidCredentials_shouldSetCurrentUser() {
        boolean ok = authService.login("admin", "admin");

        assertTrue(ok);
        assertNotNull(authService.getCurrentUser());
        assertEquals("admin", authService.getCurrentUser().getLogin());
        assertEquals(Role.ADMIN, authService.getCurrentUser().getRole());
    }

    @Test
    void login_withInvalidCredentials_shouldFailAndNotSetUser() {
        boolean ok = authService.login("admin", "wrong");

        assertFalse(ok);
        assertNull(authService.getCurrentUser());
    }

    @Test
    void logout_shouldClearCurrentUser() {
        authService.login("admin", "admin");
        assertNotNull(authService.getCurrentUser());

        authService.logout();

        assertNull(authService.getCurrentUser());
    }
}
