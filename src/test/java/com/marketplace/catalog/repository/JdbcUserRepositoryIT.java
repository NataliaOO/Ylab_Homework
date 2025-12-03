package com.marketplace.catalog.repository;

import com.marketplace.catalog.it.BasePgIT;
import com.marketplace.catalog.model.Role;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.repository.impl.jdbc.JdbcUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JdbcUserRepositoryIT extends BasePgIT {

    private UserRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        truncate(TBL_USERS);
        repo = new JdbcUserRepository(connectionFactory, SCHEMA);

        User admin = new User(null, "admin", "admin", Role.ADMIN);
        repo.save(admin);
    }

    @Test
    void save_shouldAssignIdAndPersist() {
        User user = new User(null, "user1", "pwd", Role.VIEWER);
        User saved = repo.save(user);

        assertNotNull(saved.getId());
        assertEquals("user1", saved.getLogin());
        assertEquals("pwd", saved.getPassword());
        assertEquals(Role.VIEWER, saved.getRole());
    }

    @Test
    void findByLogin_shouldReturnExistingUser() {
        Optional<User> adminOpt = repo.findByLogin("admin");
        assertTrue(adminOpt.isPresent());

        User admin = adminOpt.get();
        assertEquals("admin", admin.getLogin());
        assertEquals("admin", admin.getPassword());
        assertEquals(Role.ADMIN, admin.getRole());
    }

    @Test
    void findByLogin_shouldReturnEmpty_forUnknownUser() {
        assertTrue(repo.findByLogin("unknown").isEmpty());
    }
}

