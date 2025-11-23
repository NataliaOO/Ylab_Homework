package com.marketplace.catalog.db;

import com.marketplace.catalog.config.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionFactory {
    private final String url;
    private final String user;
    private final String password;

    public ConnectionFactory(Config config) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL JDBC driver not found in classpath", e);
        }
        this.url      = config.getDbUrl();
        this.user     = config.getDbUser();
        this.password = config.getDbPassword();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
