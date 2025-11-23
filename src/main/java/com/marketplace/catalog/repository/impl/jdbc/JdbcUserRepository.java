package com.marketplace.catalog.repository.impl.jdbc;

import com.marketplace.catalog.exception.RepositoryException;
import com.marketplace.catalog.model.Role;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.repository.UserRepository;
import com.marketplace.catalog.db.ConnectionFactory;

import java.sql.*;
import java.util.Optional;

/**
 * JDBC-репозиторий пользователей.
 */
public class JdbcUserRepository implements UserRepository {
    private static final String TABLE_NAME  = "users";

    // ---- Колонки -----------------------------------------------------------

    private static final String COL_ID       = "id";
    private static final String COL_LOGIN    = "login";
    private static final String COL_PASSWORD = "password";
    private static final String COL_ROLE     = "role";

    // ---- SQL ---------------------------------------------------------------

    private static final String SQL_INSERT = """
            INSERT INTO %s (%s, %s, %s)
            VALUES (?, ?, ?)
            RETURNING %s""";

    private static final String SQL_FIND_BY_LOGIN = """
            SELECT %s, %s, %s, %s
            FROM %s
            WHERE %s = ?""";

    private static final String ERR_INSERT = "Insert user failed";
    private static final String ERR_QUERY  = "Query user failed";

    private final ConnectionFactory connectionFactory;
    private final String tableUsers;
    private final String sqlFindByLogin;
    private final String sqlInsert;

    public JdbcUserRepository(ConnectionFactory connectionFactory, String schema) {
        this.connectionFactory = connectionFactory;
        this.tableUsers = schema + "." + TABLE_NAME;

        this.sqlFindByLogin = SQL_FIND_BY_LOGIN.formatted(
                COL_ID, COL_LOGIN, COL_PASSWORD, COL_ROLE,
                tableUsers,
                COL_LOGIN
        );
        this.sqlInsert = SQL_INSERT.formatted(
                tableUsers,
                COL_LOGIN, COL_PASSWORD, COL_ROLE,
                COL_ID
        );
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            return insert(user);
        }
        throw new UnsupportedOperationException("User update is not supported yet");
    }

    private User insert(User u) {
        try (Connection c = connectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlInsert)) {

            ps.setString(1, u.getLogin());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getRole() != null ? u.getRole().name() : null);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    u.setId(rs.getLong(1));
                }
            }
            return u;
        } catch (SQLException e) {
            throw new RepositoryException(ERR_INSERT, e);
        }
    }

    @Override
    public Optional<User> findByLogin(String login) {
        try (Connection c = connectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlFindByLogin)) {

            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException(ERR_QUERY, e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        Long id       = rs.getLong(COL_ID);
        String login  = rs.getString(COL_LOGIN);
        String pass   = rs.getString(COL_PASSWORD);
        String role   = rs.getString(COL_ROLE);

        Role r = null;
        if (role != null) {
            r = Role.valueOf(role);
        }
        return new User(id,login, pass, r);
    }
}