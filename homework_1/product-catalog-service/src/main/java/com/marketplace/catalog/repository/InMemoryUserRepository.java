package com.marketplace.catalog.repository;

import com.marketplace.catalog.model.Role;
import com.marketplace.catalog.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory хранилище пользователей.
 */
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> byLogin = new HashMap<>();

    /**
     * Создаёт репозиторий с двумя тестовыми пользователями:
     * admin/admin (ADMIN) и user/user (VIEWER).
     */
    public InMemoryUserRepository() {
        User admin = new User(1L, "admin", "admin", Role.ADMIN);
        User viewer = new User(2L, "user", "user", Role.VIEWER);
        byLogin.put(admin.getLogin(), admin);
        byLogin.put(viewer.getLogin(), viewer);
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return Optional.ofNullable(byLogin.get(login));
    }
}
