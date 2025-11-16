package com.marketplace.catalog.repository.impl.jdbc;

import com.marketplace.catalog.config.AppConfig;
import com.marketplace.catalog.model.Role;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.repository.UserRepository;
import com.marketplace.catalog.db.ConnectionFactory;

import java.sql.*;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {
    private final String schema = AppConfig.get("db.schema");

    @Override
    public Optional<User> findByLogin(String login) {
        final String sql = """
            SELECT id, login, password, role
              FROM %s.users
             WHERE login=?
            """.formatted(schema);
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new User(
                        rs.getLong("id"),
                        rs.getString("login"),
                        rs.getString("password"),
                        Role.valueOf(rs.getString("role"))
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Find user by login failed", e);
        }
    }

    @Override
    public User save(User u) {
        if (u.getId() == null) return insert(u);
        update(u); return u;
    }

    private User insert(User u) {
        final String sql = """
            INSERT INTO %s.users (id, login, password, role)
            VALUES (nextval('%s.user_seq'), ?, ?, ?)
            RETURNING id
            """.formatted(schema, schema);
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getLogin());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getRole().name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) u.setId(rs.getLong(1));
            }
            return u;
        } catch (SQLException e) {
            throw new RuntimeException("Insert user failed", e);
        }
    }

    private void update(User u) {
        final String sql = """
            UPDATE %s.users SET login=?, password=?, role=? WHERE id=?
            """.formatted(schema);
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getLogin());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getRole().name());
            ps.setLong(4, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update user failed", e);
        }
    }
}